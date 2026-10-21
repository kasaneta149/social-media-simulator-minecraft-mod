package com.kasaneta.fakesocialsimulator;

import java.util.Locale;

public final class SocialFormat {

    private SocialFormat() {}

    public static String compact(long count) {
        if (count < 10000) return String.valueOf(count);

        if (count < 100000) {
            double val = count / 1000.0;
            return String.format(Locale.US, "%.1f", val).replace(".0", "") + "k";
        } else if (count < 1000000) {
            return (count / 1000) + "k";
        } else if (count < 1000000000L) {
            return String.format(Locale.US, "%.2f", count / 1000000.0) + "m";
        } else if (count < 1000000000000L) {
            return String.format(Locale.US, "%.2f", count / 1000000000.0) + "b";
        } else {
            return String.format(Locale.US, "%.2f", count / 1000000000000.0) + "t";
        }
    }

    public static String grouped(long count, String separator) {
        String digits = Long.toString(Math.abs(count));
        if (digits.length() <= 3) return count < 0 ? "-" + digits : digits;

        StringBuilder sb = new StringBuilder();
        int first = digits.length() % 3;
        if (first > 0) sb.append(digits, 0, first);
        for (int i = first; i < digits.length(); i += 3) {
            if (sb.length() > 0) sb.append(separator);
            sb.append(digits, i, i + 3);
        }
        return count < 0 ? "-" + sb : sb.toString();
    }
}
