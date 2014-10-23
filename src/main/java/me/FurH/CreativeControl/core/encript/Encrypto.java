package me.FurH.CreativeControl.core.encript;

import java.util.*;
import me.FurH.CreativeControl.core.exceptions.*;
import java.security.*;
import me.FurH.CreativeControl.core.file.*;
import java.io.*;

public class Encrypto
{
    public static String salt(final String algorithm, final int length) throws CoreException {
        String hash;
        for (hash = ""; length > hash.length(); hash += hash(algorithm, UUID.randomUUID().toString())) {}
        return hash.substring(0, length);
    }
    
    public static String hash(final String algorithm, final String string) throws CoreException {
        if (algorithm.equalsIgnoreCase("whirl-pool")) {
            return Whirlpool.display(digest(algorithm, string));
        }
        return hex(digest(algorithm, string));
    }
    
    public static String hash(final String algorithm, final File file) throws CoreException {
        if (algorithm.equalsIgnoreCase("whirl-pool")) {
            throw new CoreException("whirlpool is not supported with files!");
        }
        return hex(digest(algorithm, file));
    }
    
    public static byte[] digest(final String algorithm, final String string) throws CoreException {
        if (algorithm.equalsIgnoreCase("whirl-pool")) {
            return whirlpool(string);
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new CoreException(ex, "There is no algorithm called: " + algorithm);
        }
        return md.digest(string.getBytes());
    }
    
    public static byte[] digest(final String algorithm, final File file) throws CoreException {
        if (algorithm.equalsIgnoreCase("whirl-pool")) {
            throw new CoreException("whirlpool is not supported with files!");
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new CoreException(ex, "There is no algorithm called: " + algorithm);
        }
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            final byte[] data = new byte[1024];
            for (int read = is.read(data, 0, 1024); read > -1; read = is.read(data, 0, 1024)) {
                md.update(data, 0, read);
            }
        }
        catch (Exception ex2) {
            throw new CoreException(ex2, "Failed to generate '" + algorithm + "' hash to the '" + file.getName() + "' file");
        }
        finally {
            FileUtils.closeQuietly(is);
        }
        return md.digest();
    }
    
    private static byte[] whirlpool(final String string) {
        final Whirlpool whirlpool = new Whirlpool();
        final byte[] digest = new byte[64];
        whirlpool.NESSIEinit();
        whirlpool.NESSIEadd(string);
        whirlpool.NESSIEfinalize(digest);
        return digest;
    }
    
    public static String hex(final byte[] data) {
        String result = "";
        for (int i = 0; i < data.length; ++i) {
            result += Integer.toString((data[i] & 0xFF) + 256, 16).substring(1);
        }
        return result;
    }
    
    public static String hex(final byte[] data, final int supress) {
        String result = "";
        for (int i = 0; i < data.length; ++i) {
            if (i % supress > 0) {
                result += Integer.toString((data[i] & 0xFF) + 256, 16).substring(1);
            }
        }
        return result;
    }
}
