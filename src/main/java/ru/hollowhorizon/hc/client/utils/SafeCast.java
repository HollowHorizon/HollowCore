package ru.hollowhorizon.hc.client.utils;

public class SafeCast {
    public static <R, T extends R> R safeCast(T obj) {
        return obj;
    }
}
