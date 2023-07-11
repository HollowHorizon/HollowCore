package ru.hollowhorizon.hc.client.utils;

public class JavaHacks {
    public static <R, K extends R> K forceCast(R original) {
        return (K) original;
    }
}
