package org.example.xchange.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AppUtils {

    public static final String EUR = "EUR";
    public static String APP_DATE_FORMAT = "yyyy-MM-dd";


    public static String getCurrentDateInString() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(APP_DATE_FORMAT);
        return currentDate.format(formatter);
    }
}
