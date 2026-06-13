package com.linkmesh.util;

public final class Base62Encoder {

    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;

    private Base62Encoder() {}

    public static String encode(long value) {
        if (value == 0) return String.valueOf(ALPHABET.charAt(0));
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(ALPHABET.charAt((int) (value % BASE)));
            value /= BASE;
        }
        return sb.reverse().toString();
    }

    public static String encodeFixedLength(long value, int length) {
        String encoded = encode(value);
        if (encoded.length() >= length)
            return encoded.substring(encoded.length() - length);
        return "0".repeat(length - encoded.length()) + encoded;
    }
}