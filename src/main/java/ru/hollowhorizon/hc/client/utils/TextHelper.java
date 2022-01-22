package ru.hollowhorizon.hc.client.utils;

import java.util.ArrayList;
import java.util.List;

public class TextHelper {
    public static List<String> splitString(String text) {
        List<String> s = splitString(text, 42, 3);

        //поддержка цвета текста при переносе строки
        if (s.size() > 0) {
            if (s.get(0).startsWith("§")) {
                if (s.size() > 1) s.set(1, s.get(0).substring(0, 2) + s.get(1));
                if (s.size() > 2) s.set(2, s.get(0).substring(0, 2) + s.get(2));
            }
        }

        return s;
    }

    public static List<String> splitString(String string, int mediumLength, int maxSize) {
        String[] split = string.split(" ");
        List<String> out = new ArrayList<>();
        int i = 0;
        StringBuilder builder = new StringBuilder();
        while (i < split.length && out.size() < maxSize) {
            builder.append(split[i]).append(" ");
            if (builder.toString().length() > mediumLength - 5 || i == split.length - 1) {
                out.add(builder.toString());
                builder.delete(0, builder.length());
            }
            i++;
        }
        return out;
    }
}
