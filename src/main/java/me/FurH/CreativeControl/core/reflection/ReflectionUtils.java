package me.FurH.CreativeControl.core.reflection;

import me.FurH.CreativeControl.core.reflection.field.*;
import javax.tools.*;
import java.lang.reflect.*;
import me.FurH.CreativeControl.core.exceptions.*;

public class ReflectionUtils
{
    public static IReflectField getNewReflectField(final String field, final Class<?> cls, final boolean set) {
        if (isReflectifyAvailable()) {
            return new ReflectifyField(field, cls, set);
        }
        return new DefaultField(field, cls, set);
    }
    
    public static boolean isReflectifyAvailable() {
        try {
            return Class.forName("org.abstractmeta.reflectify.ReflectifyRegistry") != null && ToolProvider.getSystemJavaCompiler() != null;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }
    
    public static void setFinalField(final Object obj, final String field, final Object value) throws CoreException {
        try {
            final Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            final Field mF = Field.class.getDeclaredField("modifiers");
            mF.setAccessible(true);
            mF.setInt(f, f.getModifiers() & 0xFFFFFFEF);
            f.set(obj, value);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to set the final field: " + field + ", of the class: " + obj.getClass().getSimpleName());
        }
    }
    
    public static Object getPrivateField(final Object obj, final String field) throws CoreException {
        try {
            final Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(obj);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to get private field data, field: " + field + ", of the class: " + obj.getClass().getSimpleName());
        }
    }
    
    public static int getPrivateIntField(final Object obj, final String field) throws CoreException {
        try {
            final Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.getInt(obj);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to get private field data, field: " + field + ", of the class: " + obj.getClass().getSimpleName());
        }
    }
    
    public static boolean getPrivateBooleanField(final Object obj, final String field) throws CoreException {
        try {
            final Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.getBoolean(obj);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to get private field data, field: " + field + ", of the class: " + obj.getClass().getSimpleName());
        }
    }
    
    public static Object getPrivateField(final Class<?> obj, final Object instance, final String field) throws CoreException {
        try {
            final Field f = obj.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(instance);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to get private field data, field: " + field + ", of the class: " + obj.getClass().getSimpleName());
        }
    }
    
    public static int getPrivateIntField(final Class<?> obj, final Object instance, final String field) throws CoreException {
        try {
            final Field f = obj.getDeclaredField(field);
            f.setAccessible(true);
            return f.getInt(instance);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to get private field data, field: " + field + ", of the class: " + obj.getClass().getSimpleName());
        }
    }
    
    public static void setPrivateField(final Object obj, final String field, final Object value) throws CoreException {
        try {
            final Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to set private field data, field: " + field + ", of the class: " + obj.getClass().getSimpleName());
        }
    }
}
