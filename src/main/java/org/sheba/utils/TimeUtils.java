package org.sheba.utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static Date decodeTime(long t) {

        int second = (int) (t % 60);
        t = t / 60;

        int minute = (int) (t % 60);
        t = t / 60;

        int hour = (int) (t % 24);
        t = t / 24;

        int day = (int) (t % 31) + 1;
        t = t / 31;

        int month = (int) (t % 12);
        t = t / 12;

        int year = (int) (t + 2000);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        return calendar.getTime();
    }

}
