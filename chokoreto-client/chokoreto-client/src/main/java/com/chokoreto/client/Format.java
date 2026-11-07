package com.chokoreto.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Helpers for turning money / time into text and back again. No Minecraft code in here. */
public final class Format {
    private Format() {}

    private static final Pattern MONEY = Pattern.compile(
            "^\\$?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?|\\.[0-9]+)\\s*([kmbtKMBT]?)$");

    /** "5m" -> 5000000. Returns -1 if the text is not a valid amount. */
    public static long parseMoney(String text) {
        if (text == null) return -1;
        Matcher m = MONEY.matcher(text.trim());
        if (!m.matches()) return -1;
        try {
            BigDecimal number = new BigDecimal(m.group(1).replace(",", ""));
            number = number.multiply(multiplier(m.group(2)));
            return number.setScale(0, RoundingMode.DOWN).longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            return -1;
        }
    }

    private static BigDecimal multiplier(String suffix) {
        switch (suffix.toLowerCase(Locale.ROOT)) {
            case "k": return BigDecimal.valueOf(1_000L);
            case "m": return BigDecimal.valueOf(1_000_000L);
            case "b": return BigDecimal.valueOf(1_000_000_000L);
            case "t": return BigDecimal.valueOf(1_000_000_000_000L);
            default:  return BigDecimal.ONE;
        }
    }

    /** 5000000 -> "5m" */
    public static String shortNumber(long value) {
        if (value >= 1_000_000_000_000L) return trim(value, 1_000_000_000_000L) + "t";
        if (value >= 1_000_000_000L) return trim(value, 1_000_000_000L) + "b";
        if (value >= 1_000_000L) return trim(value, 1_000_000L) + "m";
        if (value >= 1_000L) return trim(value, 1_000L) + "k";
        return Long.toString(value);
    }

    /** 5000000 -> "$5m" */
    public static String money(long value) {
        return "$" + shortNumber(value);
    }

    /** 5000000 -> "$5,000,000" */
    public static String moneyFull(long value) {
        return String.format(Locale.ROOT, "$%,d", value);
    }

    /** +5000000 -> "+$5m", -2000000 -> "-$2m" */
    public static String signed(long value) {
        return (value < 0 ? "-" : "+") + money(Math.abs(value));
    }

    private static String trim(long value, long divisor) {
        return BigDecimal.valueOf(value)
                .divide(BigDecimal.valueOf(divisor), 2, RoundingMode.DOWN)
                .stripTrailingZeros()
                .toPlainString();
    }

    /** Milliseconds -> "1:05" or "1:02:03" */
    public static String clock(long millis) {
        long total = Math.max(0, (millis + 999) / 1000);
        long h = total / 3600;
        long m = (total % 3600) / 60;
        long s = total % 60;
        if (h > 0) return String.format(Locale.ROOT, "%d:%02d:%02d", h, m, s);
        return String.format(Locale.ROOT, "%d:%02d", m, s);
    }

    /** Seconds -> "1 min", "2 hours", "1m 30s" */
    public static String duration(long seconds) {
        if (seconds < 60) return seconds + "s";
        if (seconds % 3600 == 0) {
            long h = seconds / 3600;
            return h + (h == 1 ? " hour" : " hours");
        }
        if (seconds % 60 == 0) return (seconds / 60) + " min";
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }

    /** Timestamp -> "just now", "5m ago", "3h ago", "4 days ago" */
    public static String ago(long timestampMs) {
        long s = Math.max(0, (System.currentTimeMillis() - timestampMs) / 1000);
        if (s < 10) return "just now";
        if (s < 60) return s + "s ago";
        if (s < 3600) return (s / 60) + "m ago";
        if (s < 86400) return (s / 3600) + "h ago";
        long d = s / 86400;
        return d + (d == 1 ? " day ago" : " days ago");
    }
}
