package com.milkdiary.app.util;

import java.util.Locale;

public class FormatUtils {

    /** Rs. 1,250.50 */
    public static String money(double amount) {
        return String.format(Locale.US, "Rs. %.2f", amount);
    }

    /** 5.50 L */
    public static String liters(double liters) {
        return String.format(Locale.US, "%.2f L", liters);
    }

    /** "5.50"  — for populating input fields (no suffix, no scientific notation) */
    public static String inputValue(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    /** "50.00"  — for rate fields */
    public static String rateValue(double rate) {
        return String.format(Locale.US, "%.2f", rate);
    }

    /** Rs. 50.00/L */
    public static String rate(double rate) {
        return String.format(Locale.US, "Rs. %.2f/L", rate);
    }

    /** "5.50" — alias kept for any existing calls */
    public static String litersShort(double liters) {
        return String.format(Locale.US, "%.2f", liters);
    }
}
