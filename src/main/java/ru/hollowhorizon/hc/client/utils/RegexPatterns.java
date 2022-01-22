package ru.hollowhorizon.hc.client.utils;

import java.util.regex.Pattern;

public class RegexPatterns {
    public static final Pattern REMOVEQUOTES = Pattern.compile("[\"](.*)[\"]");
    public static final Pattern SINGLE_WHITESPACE = Pattern.compile("\\s");
    public static final Pattern MULTIPLE_WHITESPACE = Pattern.compile("\\s+");
    public static final Pattern LETTERS = Pattern.compile("[^0-9]");
    public static final Pattern SPACE_SYMBOL = Pattern.compile(Pattern.quote(" "));
    public static final Pattern DASH_SYMBOL = Pattern.compile(Pattern.quote("-"));
    public static final Pattern DOT_SYMBOL = Pattern.compile(Pattern.quote("."));
    public static final Pattern SET_COMMA = Pattern.compile(Pattern.quote("SET ,"));
    public static final Pattern APOSTROPHE = Pattern.compile(Pattern.quote("'"));
    public static final Pattern PERCENT_SYMBOL = Pattern.compile(Pattern.quote("%"));
    public static final Pattern PLUS_SYMBOL = Pattern.compile(Pattern.quote("+"));
    public static final Pattern NEWLINE_CHAR = Pattern.compile("\n", 16);
    public static final Pattern NUMBER_ONE = Pattern.compile("1", 16);
    public static final Pattern NUMBER_TWO = Pattern.compile("2", 16);
    public static final Pattern ITEM_DOT = Pattern.compile("item.", 16);
    public static final Pattern SPACE_GEM = Pattern.compile(" Gem", 16);
}
