package com.smartbiz.util;

public final class NumeroGenerator {

    private NumeroGenerator() {
    }

    public static String generer(String prefixe, Long id) {
        return prefixe + "-" + String.format("%06d", id);
    }
}
