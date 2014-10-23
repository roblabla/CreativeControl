package me.FurH.CreativeControl.core.file;

import me.FurH.CreativeControl.core.database.*;
import java.sql.*;
import java.nio.channels.*;
import me.FurH.CreativeControl.core.exceptions.*;
import java.util.*;
import java.io.*;

public class FileUtils
{
    public static void closeQuietly(final Statement stream) {
        CoreSQLDatabase.closeQuietly(stream);
    }
    
    public static void closeQuietly(final ResultSet stream) {
        CoreSQLDatabase.closeQuietly(stream);
    }
    
    public static void closeQuietly(final Reader stream) {
        if (stream != null) {
            try {
                stream.close();
            }
            catch (Exception ex) {}
        }
    }
    
    public static void closeQuietly(final Writer stream) {
        if (stream != null) {
            try {
                stream.flush();
                stream.close();
            }
            catch (Exception ex) {}
        }
    }
    
    public static void closeQuietly(final Channel stream) {
        if (stream != null) {
            try {
                stream.close();
            }
            catch (Exception ex) {}
        }
    }
    
    public static void closeQuietly(final OutputStream stream) {
        if (stream != null) {
            try {
                stream.flush();
                stream.close();
            }
            catch (Exception ex) {}
        }
    }
    
    public static void closeQuietly(final InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            }
            catch (Exception ex) {}
        }
    }
    
    public static void closeQuietly(final Scanner stream) {
        if (stream != null) {
            try {
                stream.close();
            }
            catch (Exception ex) {}
        }
    }
    
    public static byte[] getBytesFromFile(final File file) throws CoreException {
        InputStream is = null;
        try {
            int offset = 0;
            int read = 0;
            is = new FileInputStream(file);
            final byte[] data = new byte[(int)file.length()];
            while (offset < data.length && (read = is.read(data, offset, data.length - offset)) >= 0) {
                offset += read;
                is.close();
            }
            return data;
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to read '" + file.getName() + "' bytes!");
        }
    }
    
    public static List<String> getLinesFromFile(final File file) throws CoreException {
        final List<String> ret = new ArrayList<String>();
        FileInputStream fis = null;
        Scanner scanner = null;
        try {
            fis = new FileInputStream(file);
            scanner = new Scanner(fis);
            while (scanner.hasNext()) {
                ret.add(scanner.nextLine());
            }
            return ret;
        }
        catch (FileNotFoundException ex) {
            throw new CoreException(ex, "Failed to get '" + file.getName() + "' lines!");
        }
        finally {
            closeQuietly(fis);
            closeQuietly(scanner);
        }
    }
    
    public static void setLinesOfFile(final File file, final List<String> lines) throws CoreException {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            final String l = System.getProperty("line.separator");
            fw = new FileWriter(file, false);
            bw = new BufferedWriter(fw);
            for (final String line : lines) {
                bw.write(line + l);
            }
            bw.flush();
            fw.flush();
        }
        catch (IOException ex) {
            throw new CoreException(ex, "Failed to set '" + file.getName() + "' lines!");
        }
        finally {
            closeQuietly(fw);
            closeQuietly(bw);
        }
    }
    
    public static void copyFile(final InputStream in, final File file) throws CoreException {
        OutputStream out = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file);
            final byte[] buffer = new byte[512];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
        catch (IOException ex) {
            throw new CoreException(ex, "Failed to copy the file '" + file.getName() + "' to '" + file.getAbsolutePath() + "'");
        }
        finally {
            closeQuietly(out);
        }
    }
}
