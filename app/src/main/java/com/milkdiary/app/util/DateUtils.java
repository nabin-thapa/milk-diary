package com.milkdiary.app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static final String FORMAT_DB = "yyyy-MM-dd";
    public static final String FORMAT_DISPLAY = "dd MMM yyyy";
    public static final String FORMAT_MONTH_DB = "yyyy-MM";
    public static final String FORMAT_MONTH_DISPLAY = "MMMM yyyy";

    public static String todayDb() {
        return new SimpleDateFormat(FORMAT_DB, Locale.getDefault()).format(new Date());
    }

    public static String currentMonthDb() {
        return new SimpleDateFormat(FORMAT_MONTH_DB, Locale.getDefault()).format(new Date());
    }

    /** Convert "yyyy-MM-dd" → "dd MMM yyyy" for display */
    public static String dbToDisplay(String dbDate) {
        try {
            Date d = new SimpleDateFormat(FORMAT_DB, Locale.getDefault()).parse(dbDate);
            return new SimpleDateFormat(FORMAT_DISPLAY, Locale.getDefault()).format(d);
        } catch (ParseException | NullPointerException e) {
            return dbDate;
        }
    }

    /** Convert "yyyy-MM" → "MMMM yyyy" for display */
    public static String monthDbToDisplay(String yearMonth) {
        try {
            Date d = new SimpleDateFormat(FORMAT_MONTH_DB, Locale.getDefault()).parse(yearMonth);
            return new SimpleDateFormat(FORMAT_MONTH_DISPLAY, Locale.getDefault()).format(d);
        } catch (ParseException | NullPointerException e) {
            return yearMonth;
        }
    }

    public static int[] getYearMonth(String yearMonth) {
        String[] parts = yearMonth.split("-");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    public static String buildYearMonth(int year, int month) {
        return String.format(Locale.getDefault(), "%04d-%02d", year, month);
    }

    public static String calendarToDb(Calendar cal) {
        return new SimpleDateFormat(FORMAT_DB, Locale.getDefault()).format(cal.getTime());
    }
}
