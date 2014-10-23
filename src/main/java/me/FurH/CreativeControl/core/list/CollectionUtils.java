package me.FurH.CreativeControl.core.list;

import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.core.number.*;
import java.util.*;

public class CollectionUtils
{
    public static HashSet<String> toStringHashSet(String string, final String split) throws CoreException {
        final HashSet<String> set = new HashSet<String>();
        try {
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");
            if (string.contains(split) && !"[]".equals(string)) {
                set.addAll(Arrays.asList(string.split(split)));
            }
            else if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                set.add(string);
            }
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse string '" + string + " into a HashSet spliting at '" + split + "'");
        }
        return set;
    }
    
    public static List<String> toStringList(String string, final String split) throws CoreException {
        final List<String> set = new ArrayList<String>();
        try {
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");
            if (string.contains(split) && !"[]".equals(string)) {
                set.addAll(Arrays.asList(string.split(split)));
            }
            else if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                set.add(string);
            }
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse string '" + string + " into a ArrayList spliting at '" + split + "'");
        }
        return set;
    }
    
    public static HashSet<Integer> toIntegerHashSet(String string, final String split) throws CoreException {
        final HashSet<Integer> set = new HashSet<Integer>();
        try {
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");
            if (string.contains(split) && !"[]".equals(string)) {
                final String[] arr$;
                final String[] splits = arr$ = string.split(split);
                for (final String str : arr$) {
                    if (NumberUtils.isInteger(str)) {
                        set.add(NumberUtils.toInteger(str));
                    }
                }
            }
            else if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                set.add(Integer.parseInt(string));
            }
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse string '" + string + " into a HashSet spliting at '" + split + "'");
        }
        return set;
    }
    
    public static List<Integer> toIntegerList(String string, final String split) throws CoreException {
        final List<Integer> set = new ArrayList<Integer>();
        try {
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");
            if (string.contains(split) && !"[]".equals(string)) {
                final String[] arr$;
                final String[] splits = arr$ = string.split(split);
                for (final String str : arr$) {
                    if (NumberUtils.isInteger(str)) {
                        set.add(NumberUtils.toInteger(str));
                    }
                }
            }
            else if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                set.add(Integer.parseInt(string));
            }
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse string '" + string + " into a ArrayList spliting at '" + split + "'");
        }
        return set;
    }
    
    public static List<Integer> getIntegerList(final Object object) {
        final List<Integer> list = new ArrayList<Integer>();
        if (object instanceof List) {
            final List<?> old = (List<?>)object;
            if (old == null) {
                return list;
            }
            if (object instanceof String && object.toString().equals("[]")) {
                return list;
            }
            for (final Object value : old) {
                if (value instanceof Number) {
                    list.add(((Number)value).intValue());
                }
            }
        }
        return list;
    }
    
    public static List<String> getStringList(final Object object) {
        final List<String> list = new ArrayList<String>();
        if (object instanceof List) {
            final List<?> old = (List<?>)object;
            if (old == null) {
                return list;
            }
            if (object instanceof String && object.toString().equals("[]")) {
                return list;
            }
            for (final Object value : old) {
                if (value instanceof String || isPrimitiveWrapper(value)) {
                    list.add(String.valueOf(value));
                }
            }
        }
        return list;
    }
    
    private static boolean isPrimitiveWrapper(final Object input) {
        return input instanceof Integer || input instanceof Boolean || input instanceof Character || input instanceof Byte || input instanceof Short || input instanceof Double || input instanceof Long || input instanceof Float;
    }
}
