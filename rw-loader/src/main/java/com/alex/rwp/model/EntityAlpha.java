package com.alex.rwp.model;

import java.time.Instant;

public record EntityAlpha(
    Long id,
    String alphaName,
    String alphaCode,
    Integer alphaCount,
    Long alphaValue,
    Integer alphaScore,
    Long alphaAmount,
    String alphaDescription,
    String alphaStatus,
    Instant alphaCreatedAt,
    Instant alphaUpdatedAt
) {}
