package me.FurH.CreativeControl.core.number;

import me.FurH.CreativeControl.core.exceptions.*;

public class NumberUtils
{
    public static boolean isInteger(final String string) {
        try {
            Integer.parseInt(string);
        }
        catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    public static boolean isDouble(final String string) {
        try {
            Double.parseDouble(string);
        }
        catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    public static boolean isLong(final String string) {
        try {
            Long.parseLong(string);
        }
        catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    public static boolean isByte(final String string) {
        try {
            Byte.parseByte(string);
        }
        catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    public static boolean isShort(final String string) {
        try {
            Short.parseShort(string);
        }
        catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    public static boolean isFloat(final String string) {
        try {
            Float.parseFloat(string);
        }
        catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    public static int toInteger(final String str) throws CoreException {
        int ret = 0;
        try {
            ret = Integer.parseInt(str.replaceAll("[^0-9-]", ""));
        }
        catch (Exception ex) {
            throw new CoreException(ex, str + " is not a valid integer!");
        }
        return ret;
    }
    
    public static double toDouble(final String str) throws CoreException {
        double ret = 0.0;
        try {
            ret = Double.parseDouble(str.replaceAll("[^0-9-.]", ""));
        }
        catch (Exception ex) {
            throw new CoreException(ex, str + " is not a valid double!");
        }
        return ret;
    }
    
    public static long toLong(final String str) throws CoreException {
        long ret = 0L;
        try {
            ret = Long.parseLong(str.replaceAll("[^0-9-]", ""));
        }
        catch (Exception ex) {
            throw new CoreException(ex, str + " is not a valid long!");
        }
        return ret;
    }
    
    public static byte toByte(final String str) throws CoreException {
        byte ret = 0;
        try {
            ret = Byte.parseByte(str.replaceAll("[^0-9-]", ""));
        }
        catch (Exception ex) {
            throw new CoreException(ex, str + " is not a valid byte!");
        }
        return ret;
    }
    
    public static short toShort(final String str) throws CoreException {
        short ret = 0;
        try {
            ret = Short.parseShort(str.replaceAll("[^0-9-]", ""));
        }
        catch (Exception ex) {
            throw new CoreException(ex, str + " is not a valid short!");
        }
        return ret;
    }
    
    public static float toFloat(final String str) throws CoreException {
        float ret = 0.0f;
        try {
            ret = Float.parseFloat(str.replaceAll("[^0-9-]", ""));
        }
        catch (Exception ex) {
            throw new CoreException(ex, str + " is not a valid float!");
        }
        return ret;
    }
}
