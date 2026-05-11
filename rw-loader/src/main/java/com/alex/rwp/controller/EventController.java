package com.alex.rwp.controller;

import com.alex.rwp.model.Event;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Stream events into RisingWave")
public class EventController {

    private final JdbcTemplate jdbc;
    private final AtomicLong idGen = new AtomicLong(System.currentTimeMillis());

    public EventController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    @Operation(summary = "List all events")
    public List<Event> list() {
        return jdbc.query(
            "SELECT id, type, payload, created_at FROM events ORDER BY created_at DESC",
            (rs, row) -> new Event(
                rs.getLong("id"),
                rs.getString("type"),
                rs.getString("payload"),
                rs.getTimestamp("created_at").toInstant()
            )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by id")
    public ResponseEntity<Event> get(@PathVariable Long id) {
        return jdbc.query(
            "SELECT id, type, payload, created_at FROM events WHERE id = ?",
            (rs, row) -> new Event(
                rs.getLong("id"),
                rs.getString("type"),
                rs.getString("payload"),
                rs.getTimestamp("created_at").toInstant()
            ),
            id
        ).stream().findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Insert an event")
    public ResponseEntity<Event> create(@RequestBody Map<String, String> body) {
        long id = idGen.incrementAndGet();
        Instant now = Instant.now();
        jdbc.update(
            "INSERT INTO events (id, type, payload, created_at) VALUES (?, ?, ?, ?)",
            id, body.get("type"), body.get("payload"), Timestamp.from(now)
        );
        return get(id);
    }
}
