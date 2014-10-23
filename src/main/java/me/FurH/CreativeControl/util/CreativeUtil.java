package me.FurH.CreativeControl.util;

import org.bukkit.block.*;
import me.FurH.CreativeControl.*;
import me.FurH.CreativeControl.configuration.*;
import java.util.*;
import me.FurH.CreativeControl.core.list.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.core.number.*;
import me.FurH.CreativeControl.core.time.*;

public class CreativeUtil
{
    public static boolean isBlackListedSign(final Sign sign) {
        final String line1 = removeCodes(sign.getLine(0).replaceAll(" ", "_"));
        final String line2 = removeCodes(sign.getLine(1).replaceAll(" ", "_"));
        final String line3 = removeCodes(sign.getLine(2).replaceAll(" ", "_"));
        final String line4 = removeCodes(sign.getLine(3).replaceAll(" ", "_"));
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(sign.getWorld());
        return config.black_sign.contains(line1) || config.black_sign.contains(line2) || config.black_sign.contains(line3) || config.black_sign.contains(line4);
    }
    
    private static String removeCodes(final String line) {
        return line.toLowerCase().replaceAll("§([0-9a-fk-or])", "").replaceAll("[^a-zA-Z0-9]", "");
    }
    
    public static HashSet<String> toStringHashSet(final String string, final String split) {
        try {
            return CollectionUtils.toStringHashSet(string, split);
        }
        catch (CoreException ex) {
            CreativeControl.plugin.getCommunicator().error(ex);
            return null;
        }
    }
    
    public static HashSet<Integer> toIntegerHashSet(final String string, final String split) {
        try {
            return CollectionUtils.toIntegerHashSet(string, split);
        }
        catch (CoreException ex) {
            CreativeControl.plugin.getCommunicator().error(ex);
            return null;
        }
    }
    
    public static int toInteger(final String str) {
        try {
            return NumberUtils.toInteger(str);
        }
        catch (CoreException ex) {
            CreativeControl.plugin.getCommunicator().error(ex);
            return 0;
        }
    }
    
    public static double toDouble(final String str) {
        try {
            return NumberUtils.toDouble(str);
        }
        catch (CoreException ex) {
            CreativeControl.plugin.getCommunicator().error(ex);
            return 0.0;
        }
    }
    
    public static String getSimpleDate(final long date) {
        return TimeUtils.getSimpleFormatedTime(date);
    }
    
    public static String getDate(final long date) {
        return TimeUtils.getFormatedTime(date);
    }
}
