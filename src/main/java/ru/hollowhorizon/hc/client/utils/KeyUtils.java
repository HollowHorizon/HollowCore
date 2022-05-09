package ru.hollowhorizon.hc.client.utils;

public class KeyUtils {
    public static final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static char getKeyByCode(int code) {
        if (code < 65 || code > 90) return 'a';
        return chars.charAt(code - 65);
    }
}
