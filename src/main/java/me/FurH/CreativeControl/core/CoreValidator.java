package me.FurH.CreativeControl.core;

import java.util.*;
import java.net.*;
import java.io.*;

public class CoreValidator
{
    public static boolean isAllowedServer() {
        boolean ret = false;
        try {
            final URL url = new URL("http://localhost/core/validate.php?ip=" + getServerIp());
            final URLConnection con = url.openConnection();
            final InputStream stream = con.getInputStream();
            final Scanner scanner = new Scanner(stream);
            if (scanner.findInLine("yes") != null) {
                ret = true;
            }
            scanner.close();
            stream.close();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return ret;
    }
    
    private static String getServerIp() {
        String ret = "127.0.0.1";
        try {
            final URL url = new URL("http://localhost/core/index.php");
            final URLConnection con = url.openConnection();
            final InputStream stream = con.getInputStream();
            final Scanner scanner = new Scanner(stream);
            if (scanner.hasNext()) {
                ret = scanner.nextLine();
            }
            if (ret.equalsIgnoreCase("localhost") || ret.equalsIgnoreCase("0.0.0.0") || ret.equalsIgnoreCase("::1")) {
                ret = "127.0.0.1";
            }
            scanner.close();
            stream.close();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return ret;
    }
}
