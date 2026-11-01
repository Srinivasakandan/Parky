package com.smartparking.util;

import org.springframework.jdbc.support.KeyHolder;

public class KeyHolderUtils {

    /**
     * Safely extracts generated auto-increment primary key ID across MySQL, Oracle, and H2
     * without throwing InvalidDataAccessApiUsageException when multiple generated keys exist.
     */
    public static Long extractGeneratedId(KeyHolder keyHolder) {
        if (keyHolder == null) return null;

        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            for (String key : keyHolder.getKeys().keySet()) {
                if ("id".equalsIgnoreCase(key)) {
                    Object val = keyHolder.getKeys().get(key);
                    if (val instanceof Number num) {
                        return num.longValue();
                    }
                }
            }
            for (Object val : keyHolder.getKeys().values()) {
                if (val instanceof Number num) {
                    return num.longValue();
                }
            }
        }

        try {
            Number num = keyHolder.getKey();
            if (num != null) return num.longValue();
        } catch (Exception ignored) {}

        return null;
    }
}

