package com.yabaipanda.mangaupdater.chapter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class Parser {
    static int Int(String val) {
        return Int(val, 0);
    }

    static int Int(String val, int defaultVal) {
        val = val.trim();
        return val.isEmpty() ? defaultVal : Integer.parseInt(val);
    }

    static LocalDate Date(String dateStr) {
        return Date(dateStr, null);
    }

    static LocalDate Date(String dateStr, LocalDate defaultVal) {
        if (dateStr == null || dateStr.trim().isEmpty()) return defaultVal;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(dateStr.trim());
        } catch (Exception ignored) {
        }
        if (parsedDate == null || parsedDate.toInstant() == null) return defaultVal;
        return parsedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
