package me.FurH.CreativeControl.core.reflection.field;

import org.abstractmeta.reflectify.runtime.*;
import org.abstractmeta.reflectify.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.core.reflection.*;

public class ReflectifyField extends IReflectField
{
    private static ReflectifyRegistry registry;
    private final Accessor accessor;
    
    public ReflectifyField(final String field, final Class<?> cls, final boolean set) {
        super(field, cls, set);
        if (ReflectifyField.registry == null) {
            ReflectifyField.registry = (ReflectifyRegistry)new ReflectifyRuntimeRegistry();
        }
        final Reflectify<?> reflectify = (Reflectify<?>)ReflectifyField.registry.get((Class)cls);
        this.accessor = reflectify.getAccessor(field);
    }
    
    public int getInt(final Object handle) throws CoreException {
        return (int)this.accessor.get(handle);
    }
    
    public boolean getBoolean(final Object handle) throws CoreException {
        return (boolean)this.accessor.get(handle);
    }
    
    public byte[] getByteArray(final Object handle) throws CoreException {
        return (byte[])this.accessor.get(handle);
    }
    
    public int[] getIntArray(final Object handle) throws CoreException {
        return (int[])this.accessor.get(handle);
    }
    
    public byte[][] getDoubleByteArray(final Object handle) throws CoreException {
        return (byte[][])this.accessor.get(handle);
    }
    
    public void set(final Object value, final Object handle) throws CoreException {
        ReflectionUtils.setPrivateField(handle, this.field, value);
    }
}
