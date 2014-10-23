package me.FurH.CreativeControl.core.ip;

import java.util.regex.*;
import org.bukkit.entity.*;

public class IpUtils
{
    private static Pattern IPv4;
    private static Pattern IPv6;
    
    public static boolean isIPv4(final String ip) {
        return IpUtils.IPv4.matcher(ip).matches();
    }
    
    public static boolean isIPv6(final String ip) {
        return IpUtils.IPv6.matcher(ip).matches();
    }
    
    public static String getIpFromDb(final String ip) {
        return ip.replaceAll("-", ".");
    }
    
    public static String getIpToDb(final String ip) {
        return ip.replaceAll("\\.", "-");
    }
    
    public static long ipToDecimal(final String ip) {
        long result = 0L;
        final String[] split = ip.split("\\.");
        for (int i = 3; i >= 0; --i) {
            result |= Long.parseLong(split[3 - i]) << i * 8;
        }
        return result & -1L;
    }
    
    public static String decimalToIp(long ip) {
        final StringBuilder sb = new StringBuilder(15);
        for (int i = 0; i < 4; ++i) {
            sb.insert(0, Long.toString(ip & 0xFFL));
            if (i < 3) {
                sb.insert(0, '.');
            }
            ip >>= 8;
        }
        return sb.toString();
    }
    
    public static String getPlayerIp(final Player player) {
        return player.getAddress().toString().substring(1, player.getAddress().toString().indexOf(58));
    }
    
    static {
        IpUtils.IPv4 = Pattern.compile("((\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d{1,5})?)|(\\d{1,3}(?:\\,\\d{1,3}){3}(?::\\d{1,5})?)|(\\d{1,3}(?:\\-\\d{1,3}){3}(?::\\d{1,5})?)|(\\d{1,3}(?: \\d{1,3}){3}(?::\\d{1,5})?))");
        IpUtils.IPv6 = Pattern.compile("(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}");
    }
}
