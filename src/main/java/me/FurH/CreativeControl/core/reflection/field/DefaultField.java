package me.FurH.CreativeControl.core.reflection.field;

import java.lang.reflect.*;
import me.FurH.CreativeControl.core.exceptions.*;

public class DefaultField extends IReflectField
{
    private Field javaField;
    
    public DefaultField(final String field, final Class<?> cls, final boolean set) {
        super(field, cls, set);
        try {
            (this.javaField = cls.getDeclaredField(field)).setAccessible(true);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public int getInt(final Object handle) throws CoreException {
        try {
            return this.javaField.getInt(handle);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to get field '" + this.field + "' from class '" + handle.getClass().getSimpleName() + "'");
        }
    }
    
    public boolean getBoolean(final Object handle) throws CoreException {
        try {
            return this.javaField.getBoolean(handle);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to get field '" + this.field + "' from class '" + handle.getClass().getSimpleName() + "'");
        }
    }
    
    public byte[] getByteArray(final Object handle) throws CoreException {
        try {
            return (byte[])this.javaField.get(handle);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to get field '" + this.field + "' from class '" + handle.getClass().getSimpleName() + "'");
        }
    }
    
    public int[] getIntArray(final Object handle) throws CoreException {
        try {
            return (int[])this.javaField.get(handle);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to get field '" + this.field + "' from class '" + handle.getClass().getSimpleName() + "'");
        }
    }
    
    public byte[][] getDoubleByteArray(final Object handle) throws CoreException {
        try {
            return (byte[][])this.javaField.get(handle);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to get field '" + this.field + "' from class '" + handle.getClass().getSimpleName() + "'");
        }
    }
    
    public void set(final Object value, final Object handle) throws CoreException {
        try {
            this.javaField.set(value, handle);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to set field '" + this.field + "' from class '" + handle.getClass().getSimpleName() + "'");
        }
    }
}
