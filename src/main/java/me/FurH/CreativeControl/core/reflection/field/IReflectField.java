package me.FurH.CreativeControl.core.reflection.field;

import me.FurH.CreativeControl.core.exceptions.*;

public abstract class IReflectField
{
    protected final String field;
    
    public IReflectField(final String field, final Class<?> cls, final boolean set) {
        super();
        this.field = field;
    }
    
    public abstract int getInt(final Object p0) throws CoreException;
    
    public abstract boolean getBoolean(final Object p0) throws CoreException;
    
    public abstract byte[] getByteArray(final Object p0) throws CoreException;
    
    public abstract int[] getIntArray(final Object p0) throws CoreException;
    
    public abstract byte[][] getDoubleByteArray(final Object p0) throws CoreException;
    
    public abstract void set(final Object p0, final Object p1) throws CoreException;
}
