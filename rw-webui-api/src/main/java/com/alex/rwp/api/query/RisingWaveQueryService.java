package com.alex.rwp.api.query;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RisingWaveQueryService {

    private final JdbcTemplate jdbc;

    public RisingWaveQueryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public PagedResult searchAlphaBeta(int startRow, int endRow, String alphaName, String betaTitle) {
        long t0 = System.currentTimeMillis();

        var sql = new StringBuilder("""
            SELECT
                a.id               AS alpha_id,
                a.alpha_name,
                a.alpha_code,
                a.alpha_count,
                a.alpha_value,
                a.alpha_score,
                a.alpha_amount,
                a.alpha_description,
                a.alpha_status,
                a.alpha_created_at,
                a.alpha_updated_at,
                r.created_at       AS ref_created_at,
                b.id               AS beta_id,
                b.beta_title,
                b.beta_ref_code,
                b.beta_quantity,
                b.beta_total,
                b.beta_priority,
                b.beta_sequence,
                b.beta_weight,
                b.beta_checksum,
                b.beta_notes,
                b.beta_label,
                b.beta_category,
                b.beta_tag,
                b.beta_created_at,
                b.beta_updated_at,
                b.beta_expires_at,
                COUNT(*) OVER()    AS _total_count
            FROM entity_alpha a
            JOIN alpha_beta_ref r ON r.alpha_id = a.id
            JOIN entity_beta    b ON b.id = r.beta_id
            WHERE 1=1
            """);

        var params = new ArrayList<>();
        if (alphaName != null && !alphaName.isBlank()) {
            sql.append(" AND a.alpha_name LIKE ?");
            params.add("%" + alphaName + "%");
        }
        if (betaTitle != null && !betaTitle.isBlank()) {
            sql.append(" AND b.beta_title LIKE ?");
            params.add("%" + betaTitle + "%");
        }
        sql.append(" ORDER BY a.id, b.id LIMIT ? OFFSET ?");
        params.add(endRow - startRow);
        params.add(startRow);

        return execute(sql.toString(), params, t0);
    }

    public PagedResult searchAlpha(int startRow, int endRow, String alphaName) {
        long t0 = System.currentTimeMillis();

        var sql = new StringBuilder("""
            SELECT
                id,
                alpha_name,
                alpha_code,
                alpha_count,
                alpha_value,
                alpha_score,
                alpha_amount,
                alpha_description,
                alpha_status,
                alpha_created_at,
                alpha_updated_at,
                COUNT(*) OVER() AS _total_count
            FROM entity_alpha
            WHERE 1=1
            """);

        var params = new ArrayList<>();
        if (alphaName != null && !alphaName.isBlank()) {
            sql.append(" AND alpha_name LIKE ?");
            params.add("%" + alphaName + "%");
        }
        sql.append(" ORDER BY id LIMIT ? OFFSET ?");
        params.add(endRow - startRow);
        params.add(startRow);

        return execute(sql.toString(), params, t0);
    }

    public PagedResult searchBeta(int startRow, int endRow, String betaTitle) {
        long t0 = System.currentTimeMillis();

        var sql = new StringBuilder("""
            SELECT
                id,
                beta_title,
                beta_ref_code,
                beta_quantity,
                beta_total,
                beta_priority,
                beta_sequence,
                beta_weight,
                beta_checksum,
                beta_notes,
                beta_label,
                beta_category,
                beta_tag,
                beta_created_at,
                beta_updated_at,
                beta_expires_at,
                COUNT(*) OVER() AS _total_count
            FROM entity_beta
            WHERE 1=1
            """);

        var params = new ArrayList<>();
        if (betaTitle != null && !betaTitle.isBlank()) {
            sql.append(" AND beta_title LIKE ?");
            params.add("%" + betaTitle + "%");
        }
        sql.append(" ORDER BY id LIMIT ? OFFSET ?");
        params.add(endRow - startRow);
        params.add(startRow);

        return execute(sql.toString(), params, t0);
    }

    private PagedResult execute(String sql, List<Object> params, long t0) {
        List<Map<String, Object>> rows = jdbc.query(sql, rs -> {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            String[] names = new String[cols + 1];
            for (int i = 1; i <= cols; i++) names[i] = meta.getColumnLabel(i);

            var result = new ArrayList<Map<String, Object>>(64);
            while (rs.next()) {
                var row = new LinkedHashMap<String, Object>(cols);
                for (int i = 1; i <= cols; i++) {
                    Object val = rs.getObject(i);
                    if (val instanceof Timestamp ts) val = ts.toInstant().toString();
                    row.put(names[i], val);
                }
                result.add(row);
            }
            return result;
        }, params.toArray());

        long totalCount = 0;
        if (rows != null && !rows.isEmpty()) {
            Object tc = rows.get(0).get("_total_count");
            if (tc instanceof Number n) totalCount = n.longValue();
            rows.forEach(r -> r.remove("_total_count"));
        }

        return new PagedResult(rows != null ? rows : List.of(), totalCount, System.currentTimeMillis() - t0);
    }
}
