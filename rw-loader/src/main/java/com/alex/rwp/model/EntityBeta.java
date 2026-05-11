package com.alex.rwp.model;

import java.time.Instant;

public record EntityBeta(
    Long id,
    String betaTitle,
    String betaRefCode,
    Integer betaQuantity,
    Long betaTotal,
    Integer betaPriority,
    Integer betaSequence,
    Long betaWeight,
    Long betaChecksum,
    String betaNotes,
    String betaLabel,
    String betaCategory,
    String betaTag,
    Instant betaCreatedAt,
    Instant betaUpdatedAt,
    Instant betaExpiresAt
) {}
