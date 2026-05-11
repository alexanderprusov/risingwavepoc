package com.alex.rwp.model;

import java.time.Instant;

public record AlphaBetaJoin(
    Long alphaId,
    String alphaName,
    String alphaCode,
    Integer alphaCount,
    Long alphaValue,
    Integer alphaScore,
    Long alphaAmount,
    String alphaDescription,
    String alphaStatus,
    Instant alphaCreatedAt,
    Instant alphaUpdatedAt,
    Instant refCreatedAt,
    Long betaId,
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
