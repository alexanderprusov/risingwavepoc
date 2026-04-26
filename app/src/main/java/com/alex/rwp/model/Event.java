package com.alex.rwp.model;

import java.time.Instant;

public record Event(Long id, String type, String payload, Instant createdAt) {}
