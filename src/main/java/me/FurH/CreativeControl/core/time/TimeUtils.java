package me.FurH.CreativeControl.core.time;

import java.text.*;
import me.FurH.CreativeControl.core.number.*;
import java.util.*;
import me.FurH.CreativeControl.core.exceptions.*;

public class TimeUtils
{
    public static String getSimpleFormatedTime(final long time) {
        return new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(time);
    }
    
    public static String getFormatedTime(final long time) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(time);
    }
    
    public static String getFormatedTimeWithMillis(final long time) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(time);
    }
    
    public static String getSimpleFormatedTimeWithMillis(final long time) {
        return new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-SSS").format(time);
    }
    
    public static long getTimeInMillis(final String timezone, final String time) throws CoreException {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        final int i = NumberUtils.toInteger(time);
        if (time.contains("w")) {
            calendar.add(3, i);
        }
        else if (time.contains("t")) {
            calendar.add(13, i * 20);
        }
        else if (time.contains("M")) {
            calendar.add(2, i);
        }
        else if (time.contains("m")) {
            calendar.add(12, i);
        }
        else if (time.contains("h")) {
            calendar.add(10, i);
        }
        else if (time.contains("d")) {
            calendar.add(6, i);
        }
        else {
            calendar.add(14, i);
        }
        return calendar.getTimeInMillis();
    }
    
    public static long getCurrentTime(final String timezone) {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        return calendar.getTimeInMillis();
    }
}
