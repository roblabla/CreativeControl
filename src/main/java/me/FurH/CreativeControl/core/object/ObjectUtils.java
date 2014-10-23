package me.FurH.CreativeControl.core.object;

import me.FurH.CreativeControl.core.file.*;
import me.FurH.CreativeControl.core.exceptions.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import org.yaml.snakeyaml.external.biz.base64Coder.*;

public class ObjectUtils
{
    public static Object getObjectFromFile(final File file) throws CoreException {
        final List<String> line = FileUtils.getLinesFromFile(file);
        if (line.isEmpty()) {
            throw new CoreException("There is nothing written in this file: " + file.getAbsolutePath());
        }
        if (line.size() > 1) {
            throw new CoreException("This file has more than one line written: " + file.getAbsolutePath());
        }
        return getObjectFromString(line.get(0));
    }
    
    public static void saveObjectToFile(final File file, final Object object) throws CoreException {
        FileUtils.setLinesOfFile(file, new ArrayList<String>(Arrays.asList(getStringFromObject(object))));
    }
    
    public static Object[] getObjectsFromFile(final File file) throws CoreException {
        final List<String> lines = FileUtils.getLinesFromFile(file);
        if (lines.isEmpty()) {
            throw new CoreException("There is nothing written in this file: " + file.getAbsolutePath());
        }
        final List<Object> objects = new ArrayList<Object>();
        for (final String line : lines) {
            objects.add(getObjectFromString(line));
        }
        return objects.toArray();
    }
    
    public static void saveObjectsToFile(final File file, final Object[] objects) throws CoreException {
        final List<String> lines = new ArrayList<String>();
        for (final Object o : objects) {
            lines.add(getStringFromObject(o));
        }
        FileUtils.setLinesOfFile(file, lines);
    }
    
    public static String getStringFromObject(final Object object) throws CoreException {
        String ret = null;
        ByteArrayOutputStream baos = null;
        GZIPOutputStream gos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            gos = new GZIPOutputStream(baos);
            oos = new ObjectOutputStream(gos);
            oos.writeObject(object);
            oos.flush();
            gos.flush();
            baos.flush();
            ret = encode(baos.toByteArray());
        }
        catch (IOException ex) {
            throw new CoreException(ex, "Failed to parse object '" + object.getClass().getSimpleName() + "' to a string");
        }
        finally {
            FileUtils.closeQuietly(baos);
            FileUtils.closeQuietly(gos);
            FileUtils.closeQuietly(oos);
        }
        return ret;
    }
    
    public static Object getObjectFromString(final String string) throws CoreException {
        Object ret = null;
        ByteArrayInputStream bais = null;
        GZIPInputStream gis = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(decode(string));
            gis = new GZIPInputStream(bais);
            ois = new ObjectInputStream(gis);
            ret = ois.readObject();
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse string '" + string + "' into an object");
        }
        finally {
            FileUtils.closeQuietly(bais);
            FileUtils.closeQuietly(gis);
            FileUtils.closeQuietly(ois);
        }
        return ret;
    }
    
    public static String encode(final byte[] data) {
        return new String(Base64Coder.encode(data));
    }
    
    public static byte[] decode(final String string) {
        return Base64Coder.decode(string);
    }
}
