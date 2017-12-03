part of util;

bool isStringBlank(String raw) => raw == null || raw.trim().length == 0;

String trimStringToEmpty(String raw) => raw == null ? "" : raw.trim();
