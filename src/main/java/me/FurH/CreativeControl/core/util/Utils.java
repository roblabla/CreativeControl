package me.FurH.CreativeControl.core.util;

import java.util.regex.*;
import me.FurH.CreativeControl.core.exceptions.*;
import java.net.*;
import java.io.*;
import me.FurH.CreativeControl.core.*;
import java.text.*;

public class Utils
{
    private static Pattern pattern;
    
    @Deprecated
    public static long ping(final String address) throws CoreException {
        return pingServer(address);
    }
    
    public static long pingServer(final String address) throws CoreException {
        long ping = -1L;
        try {
            if (!address.contains(":")) {
                throw new CoreException("Wrong usage, port is required! Eg: 127.0.0.1:80");
            }
            final String[] hostSplit = address.split(":");
            final String host = hostSplit[0];
            int port = 80;
            try {
                port = Integer.parseInt(hostSplit[1]);
            }
            catch (Exception ex) {
                throw new CoreException(ex, hostSplit[1] + " is not a valid number!");
            }
            final InetAddress addr = InetAddress.getByName(host);
            ping = ping(new InetSocketAddress(addr, port));
        }
        catch (Exception ex2) {
            throw new CoreException(ex2, "Error on server ping!");
        }
        return ping;
    }
    
    public static long ping(final InetSocketAddress address) throws CoreException {
        long ping = -1L;
        try {
            final Socket sock = new Socket();
            final long start = System.currentTimeMillis();
            sock.connect(address, 10000);
            ping = System.currentTimeMillis() - start;
        }
        catch (IOException ex) {
            throw new CoreException(ex, "Error on server ping!");
        }
        return ping;
    }
    
    public static boolean isValidEmail(final String email) {
        return Utils.pattern.matcher(email).matches();
    }
    
    public static String getServerUptime() {
        final long time = System.currentTimeMillis() - Core.start;
        return (int)(time / 86400000L) + "d " + (int)(time / 3600000L % 24L) + "h " + (int)(time / 60000L % 60L) + "m " + (int)(time / 1000L % 60L) + "s";
    }
    
    public String drawProgressBar(final int size, final int progress) {
        final StringBuilder sb = new StringBuilder();
        String empty = "";
        String done = "";
        for (int k = 0; k < size / 2 / 100; ++k) {
            empty += " ";
            done += "||";
        }
        for (int k = 0; k < 50; ++k) {
            sb.append((progress / 2 <= k) ? empty : done);
        }
        return sb.toString();
    }
    
    public static String getFormatedBytes(final double bytes) {
        final DecimalFormat decimal = new DecimalFormat("#.##");
        if (bytes >= 1.099511627776E12) {
            return decimal.format(bytes / 1.099511627776E12) + " TB";
        }
        if (bytes >= 1.073741824E9) {
            return decimal.format(bytes / 1.073741824E9) + " GB";
        }
        if (bytes >= 1048576.0) {
            return decimal.format(bytes / 1048576.0) + " MB";
        }
        if (bytes >= 1024.0) {
            return decimal.format(bytes / 1024.0) + " KB";
        }
        return "" + (int)bytes + " bytes";
    }
    
    static {
        Utils.pattern = Pattern.compile(".+@.+\\.[a-z]+");
    }
}
