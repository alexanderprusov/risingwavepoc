package com.alex.rwp.api.query;

import java.util.List;
import java.util.Map;

public record PagedResult(List<Map<String, Object>> data, long rowCount, long elapsedMs) {}
