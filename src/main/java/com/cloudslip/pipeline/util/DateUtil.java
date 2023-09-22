package com.cloudslip.pipeline.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtil {


    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DateUtil() {

    }

    public static String getFormattedDateFromMili(Date miliDate) {
        return sdf.format(miliDate);
    }
}
