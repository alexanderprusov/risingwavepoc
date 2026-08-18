package com.alex.rwp.controller;

import com.alex.rwp.model.AlphaBetaJoin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mv-alpha-beta")
@Tag(name = "MV Alpha-Beta Search", description = "Search joined alpha-beta entities via materialized views")
public class MvAlphaBetaSearchController {

    private final JdbcTemplate jdbc;

    public MvAlphaBetaSearchController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search joined alpha-beta (MV)",
        description = "Returns joined rows from mv_alpha/mv_refs/mv_beta matching alpha_name and beta_title (substring)."
    )
    public List<AlphaBetaJoin> search(
        @RequestParam(required = false) String alphaName,
        @RequestParam(required = false) String betaTitle
    ) {
        return executeSearch(alphaName, betaTitle);
    }

    @GetMapping("/search-timed")
    @Operation(
        summary = "Search joined alpha-beta (MV, timed)",
        description = "Executes the same query as /search but discards results and returns only elapsed_ms and row_count."
    )
    public Map<String, Object> searchTimed(
        @RequestParam(required = false) String alphaName,
        @RequestParam(required = false) String betaTitle
    ) {
        long start = System.currentTimeMillis();
        List<AlphaBetaJoin> results = executeSearch(alphaName, betaTitle);
        return Map.of(
            "elapsed_ms", System.currentTimeMillis() - start,
            "row_count",  results.size()
        );
    }

    private List<AlphaBetaJoin> executeSearch(String alphaName, String betaTitle) {
        var sql = new StringBuilder("""
            SELECT
                a.id                                              AS alpha_id,
                a.alpha_name,
                a.alpha_code,
                a.alpha_count,
                a.alpha_value,
                a.alpha_score,
                a.alpha_amount,
                a.alpha_description,
                a.alpha_status,
                a.alpha_created_at::timestamptz                   AS alpha_created_at,
                a.alpha_updated_at::timestamptz                   AS alpha_updated_at,
                r.created_at::timestamptz                         AS ref_created_at,
                b.id                                              AS beta_id,
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
                b.beta_created_at::timestamptz                    AS beta_created_at,
                b.beta_updated_at::timestamptz                    AS beta_updated_at,
                b.beta_expires_at::timestamptz                    AS beta_expires_at
            FROM mv_alpha a
            JOIN mv_refs  r ON r.alpha_id = a.id
            JOIN mv_beta  b ON b.id = r.beta_id
            WHERE 1=1
            """);

        var params = new ArrayList<>();
        if (alphaName != null) {
            sql.append(" AND a.alpha_name LIKE ?");
            params.add("%" + alphaName + "%");
        }
        if (betaTitle != null) {
            sql.append(" AND b.beta_title LIKE ?");
            params.add("%" + betaTitle + "%");
        }
        sql.append(" ORDER BY a.id, b.id");

        return jdbc.query(sql.toString(), (rs, row) -> new AlphaBetaJoin(
            rs.getLong("alpha_id"),
            rs.getString("alpha_name"),
            rs.getString("alpha_code"),
            rs.getInt("alpha_count"),
            rs.getLong("alpha_value"),
            rs.getInt("alpha_score"),
            rs.getLong("alpha_amount"),
            rs.getString("alpha_description"),
            rs.getString("alpha_status"),
            rs.getTimestamp("alpha_created_at").toInstant(),
            rs.getTimestamp("alpha_updated_at") != null ? rs.getTimestamp("alpha_updated_at").toInstant() : null,
            rs.getTimestamp("ref_created_at").toInstant(),
            rs.getLong("beta_id"),
            rs.getString("beta_title"),
            rs.getString("beta_ref_code"),
            rs.getInt("beta_quantity"),
            rs.getLong("beta_total"),
            rs.getInt("beta_priority"),
            rs.getInt("beta_sequence"),
            rs.getLong("beta_weight"),
            rs.getLong("beta_checksum"),
            rs.getString("beta_notes"),
            rs.getString("beta_label"),
            rs.getString("beta_category"),
            rs.getString("beta_tag"),
            rs.getTimestamp("beta_created_at").toInstant(),
            rs.getTimestamp("beta_updated_at") != null ? rs.getTimestamp("beta_updated_at").toInstant() : null,
            rs.getTimestamp("beta_expires_at") != null ? rs.getTimestamp("beta_expires_at").toInstant() : null
        ), params.toArray());
    }
}
