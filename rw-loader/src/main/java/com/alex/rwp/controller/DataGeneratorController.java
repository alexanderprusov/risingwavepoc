package com.alex.rwp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/generate")
@Tag(name = "Data Generator", description = "Generate and insert test data into all tables")
public class DataGeneratorController {

    private static final int BATCH_SIZE = 1000;

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String[] WORDS = {
        "alpha", "beta", "gamma", "delta", "echo", "foxtrot", "hotel",
        "india", "juliet", "kilo", "lima", "mike", "november", "oscar"
    };

    private static final String SQL_ALPHA = """
        INSERT INTO entity_alpha
          (id, alpha_name, alpha_code, alpha_count, alpha_value,
           alpha_score, alpha_amount, alpha_description, alpha_status,
           alpha_created_at, alpha_updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";

    private static final String SQL_BETA = """
        INSERT INTO entity_beta
          (id, beta_title, beta_ref_code, beta_quantity, beta_total,
           beta_priority, beta_sequence, beta_weight, beta_checksum,
           beta_notes, beta_label, beta_category, beta_tag,
           beta_created_at, beta_updated_at, beta_expires_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";

    private static final String SQL_REF =
        "INSERT INTO alpha_beta_ref (alpha_id, beta_id, created_at) VALUES (?, ?, ?)";

    private static final String SQL_EVENT =
        "INSERT INTO events (id, type, payload, created_at) VALUES (?, ?, ?, ?)";

    private final JdbcTemplate jdbc;
    private final AtomicLong idGen = new AtomicLong(System.currentTimeMillis());

    public DataGeneratorController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping
    @Operation(summary = "Generate test data", description = "Inserts N records into entity_alpha, entity_beta, and alpha_beta_ref. Defaults to 10.")
    public Map<String, Object> generate(@RequestParam(defaultValue = "100000") int count) {
        var r = ThreadLocalRandom.current();
        Timestamp now = Timestamp.from(Instant.now());

        List<Long> alphaIds = new ArrayList<>(count);
        List<Long> betaIds  = new ArrayList<>(count);

        // phase 1: generate all objects in memory
        long genStart = System.currentTimeMillis();

        List<Object[]> alphaRows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            long id = idGen.incrementAndGet();
            alphaIds.add(id);
            Timestamp updatedAt = r.nextBoolean() ? now : null;
            alphaRows.add(new Object[]{
                id,
                randomWord(r) + "_" + i,
                randomChar(r),
                r.nextInt(1000),
                r.nextLong(1_000_000L),
                r.nextInt(100),
                r.nextLong(1_000_000_000L),
                "Description for alpha " + i,
                randomChar(r),
                Timestamp.from(Instant.now().minus(r.nextInt(30), ChronoUnit.DAYS)),
                updatedAt
            });
        }

        List<Object[]> betaRows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            long id = idGen.incrementAndGet();
            betaIds.add(id);
            Timestamp updatedAt  = r.nextBoolean() ? now : null;
            Timestamp expiresAt  = r.nextBoolean() ? Timestamp.from(Instant.now().plus(r.nextInt(90), ChronoUnit.DAYS)) : null;
            betaRows.add(new Object[]{
                id,
                randomWord(r) + "_" + i,
                randomChar(r),
                r.nextInt(500),
                r.nextLong(999_999L),
                r.nextInt(10),
                r.nextInt(10_000),
                r.nextLong(10_000_000L),
                r.nextLong(Long.MAX_VALUE),
                "Notes for beta " + i,
                "label_" + randomWord(r),
                randomChar(r),
                randomChar(r),
                Timestamp.from(Instant.now().minus(r.nextInt(30), ChronoUnit.DAYS)),
                updatedAt,
                expiresAt
            });
        }

        List<Object[]> refRows = new ArrayList<>();
        for (long alphaId : alphaIds) {
            int linkCount = r.nextInt(1, Math.min(4, betaIds.size() + 1));
            List<Long> shuffled = new ArrayList<>(betaIds);
            java.util.Collections.shuffle(shuffled, new java.util.Random(r.nextLong()));
            for (int i = 0; i < linkCount; i++) {
                refRows.add(new Object[]{alphaId, shuffled.get(i), now});
            }
        }
        var seen = new java.util.HashSet<String>();
        refRows.removeIf(row -> !seen.add(row[0] + ":" + row[1]));

        long genMs = System.currentTimeMillis() - genStart;

        // phase 2: execute SQL inserts
        long insertStart = System.currentTimeMillis();
        batchInsert(SQL_ALPHA, alphaRows, (ps, row) -> bindAlpha(ps, row));
        batchInsert(SQL_BETA,  betaRows,  (ps, row) -> bindBeta(ps, row));
        batchInsert(SQL_REF,   refRows,   (ps, row) -> bindRef(ps, row));
        long insertMs = System.currentTimeMillis() - insertStart;

        return Map.of(
            "entity_alpha",   count,
            "entity_beta",    count,
            "alpha_beta_ref", refRows.size(),
            "generate_ms",    genMs,
            "insert_ms",      insertMs
        );
    }

    @PostMapping("/events")
    @Operation(summary = "Generate events", description = "Inserts N records into events. Defaults to 100000.")
    public Map<String, Object> generateEvents(@RequestParam(defaultValue = "100000") int count) {
        Timestamp now = Timestamp.from(Instant.now());

        long genStart = System.currentTimeMillis();
        List<Object[]> eventRows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            eventRows.add(new Object[]{idGen.incrementAndGet(), "GENERATED", "event payload " + i, now});
        }
        long genMs = System.currentTimeMillis() - genStart;

        long insertStart = System.currentTimeMillis();
        batchInsert(SQL_EVENT, eventRows, (ps, row) -> bindEvent(ps, row));
        long insertMs = System.currentTimeMillis() - insertStart;

        return Map.of(
            "events",      count,
            "generate_ms", genMs,
            "insert_ms",   insertMs
        );
    }

    // splits rows into BATCH_SIZE chunks and executes each as a prepared-statement batch
    private void batchInsert(String sql, List<Object[]> rows, RowBinder binder) {
        for (int offset = 0; offset < rows.size(); offset += BATCH_SIZE) {
            List<Object[]> chunk = rows.subList(offset, Math.min(offset + BATCH_SIZE, rows.size()));
            jdbc.execute((java.sql.Connection con) -> con.prepareStatement(sql),
                (PreparedStatement ps) -> {
                    for (Object[] row : chunk) {
                        binder.bind(ps, row);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    return null;
                });
        }
    }

    @FunctionalInterface
    private interface RowBinder {
        void bind(PreparedStatement ps, Object[] row) throws java.sql.SQLException;
    }

    private void bindAlpha(PreparedStatement ps, Object[] r) throws java.sql.SQLException {
        ps.setLong(1,   (Long)      r[0]);
        ps.setString(2, (String)    r[1]);
        ps.setString(3, (String)    r[2]);
        ps.setInt(4,    (Integer)   r[3]);
        ps.setLong(5,   (Long)      r[4]);
        ps.setInt(6,    (Integer)   r[5]);
        ps.setLong(7,   (Long)      r[6]);
        ps.setString(8, (String)    r[7]);
        ps.setString(9, (String)    r[8]);
        ps.setTimestamp(10, (Timestamp) r[9]);
        if (r[10] != null) ps.setTimestamp(11, (Timestamp) r[10]);
        else               ps.setNull(11, Types.TIMESTAMP);
    }

    private void bindBeta(PreparedStatement ps, Object[] r) throws java.sql.SQLException {
        ps.setLong(1,   (Long)      r[0]);
        ps.setString(2, (String)    r[1]);
        ps.setString(3, (String)    r[2]);
        ps.setInt(4,    (Integer)   r[3]);
        ps.setLong(5,   (Long)      r[4]);
        ps.setInt(6,    (Integer)   r[5]);
        ps.setInt(7,    (Integer)   r[6]);
        ps.setLong(8,   (Long)      r[7]);
        ps.setLong(9,   (Long)      r[8]);
        ps.setString(10, (String)   r[9]);
        ps.setString(11, (String)   r[10]);
        ps.setString(12, (String)   r[11]);
        ps.setString(13, (String)   r[12]);
        ps.setTimestamp(14, (Timestamp) r[13]);
        if (r[14] != null) ps.setTimestamp(15, (Timestamp) r[14]);
        else               ps.setNull(15, Types.TIMESTAMP);
        if (r[15] != null) ps.setTimestamp(16, (Timestamp) r[15]);
        else               ps.setNull(16, Types.TIMESTAMP);
    }

    private void bindRef(PreparedStatement ps, Object[] r) throws java.sql.SQLException {
        ps.setLong(1,      (Long)      r[0]);
        ps.setLong(2,      (Long)      r[1]);
        ps.setTimestamp(3, (Timestamp) r[2]);
    }

    private void bindEvent(PreparedStatement ps, Object[] r) throws java.sql.SQLException {
        ps.setLong(1,      (Long)      r[0]);
        ps.setString(2,    (String)    r[1]);
        ps.setString(3,    (String)    r[2]);
        ps.setTimestamp(4, (Timestamp) r[3]);
    }

    private String randomChar(ThreadLocalRandom r) {
        return String.valueOf(CHARS.charAt(r.nextInt(CHARS.length())));
    }

    private String randomWord(ThreadLocalRandom r) {
        return WORDS[r.nextInt(WORDS.length)];
    }
}
