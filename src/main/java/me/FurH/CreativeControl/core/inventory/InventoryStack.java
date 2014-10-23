package me.FurH.CreativeControl.core.inventory;

import java.util.regex.*;
import org.bukkit.inventory.*;
import me.FurH.CreativeControl.core.internals.*;
import me.FurH.CreativeControl.core.exceptions.*;
import java.math.*;
import me.FurH.CreativeControl.core.file.*;
import org.bukkit.*;
import me.FurH.CreativeControl.core.number.*;
import org.bukkit.material.*;
import java.lang.reflect.*;
import java.io.*;
import org.yaml.snakeyaml.external.biz.base64Coder.*;

public class InventoryStack
{
    private static Pattern stringItem;
    
    public static String getStringFromItemStack(final ItemStack stack) throws CoreException {
        String ret = null;
        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        try {
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            final Class<?> compoundCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "NBTTagCompound");
            final Object compound = compoundCLS.newInstance();
            final Object craftStack = getCraftVersion(stack);
            Method save = null;
            for (final Method m : craftStack.getClass().getMethods()) {
                if (m.getName().equalsIgnoreCase("save")) {
                    save = m;
                    break;
                }
            }
            if (save == null) {
                throw new CoreException("Failed to find ItemStack 'save' method!");
            }
            final Class<?> type = save.getParameterTypes()[0];
            if (craftStack != null) {
                save.invoke(craftStack, convert(compound, type));
            }
            final Class<?> baseCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "NBTBase");
            Method a = null;
            for (final Method i : baseCLS.getDeclaredMethods()) {
                if (i.getParameterTypes().length == 2 && i.getParameterTypes()[1] == DataOutput.class) {
                    a = i;
                    break;
                }
            }
            if (a == null) {
                throw new CoreException("Failed to find NBTBase required method!");
            }
            a.invoke(null, compound, dos);
            dos.flush();
            baos.flush();
            ret = new BigInteger(1, baos.toByteArray()).toString(32);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to convert the ItemStack '" + stack.toString() + "' into a string.");
        }
        finally {
            FileUtils.closeQuietly(baos);
            FileUtils.closeQuietly(dos);
        }
        return encode(ret);
    }
    
    public static String getStringFromArray(final ItemStack[] source) throws CoreException {
        String ret = null;
        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        try {
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            final Class<?> listCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "NBTTagList");
            final Object list = listCLS.newInstance();
            Method add = null;
            for (final Method m : listCLS.getDeclaredMethods()) {
                if (m.getName().equalsIgnoreCase("add")) {
                    add = m;
                    break;
                }
            }
            if (add == null) {
                throw new CoreException("Failed to find NBTTagList add method");
            }
            final Class<?> addType = add.getParameterTypes()[0];
            final Class<?> compoundCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "NBTTagCompound");
            Method save = null;
            for (int j1 = 0; j1 < source.length; ++j1) {
                final Object craftStack = getCraftVersion(source[j1]);
                final Object compound = compoundCLS.newInstance();
                if (craftStack != null) {
                    if (save == null) {
                        for (final Method i : craftStack.getClass().getMethods()) {
                            if (i.getName().equalsIgnoreCase("save")) {
                                save = i;
                                break;
                            }
                        }
                    }
                    if (save == null) {
                        throw new CoreException("Failed to find ItemStack 'save' method!");
                    }
                    final Class<?> type = save.getParameterTypes()[0];
                    save.invoke(craftStack, convert(compound, type));
                }
                add.invoke(list, convert(compound, addType));
            }
            final Class<?> baseCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "NBTBase");
            Method a = null;
            for (final Method k : baseCLS.getDeclaredMethods()) {
                if (k.getParameterTypes().length == 2 && k.getParameterTypes()[1] == DataOutput.class) {
                    a = k;
                    break;
                }
            }
            if (a != null) {
                a.invoke(null, list, dos);
            }
            else {
                for (final Method k : listCLS.getDeclaredMethods()) {
                    if (k.getParameterTypes().length == 1 && k.getParameterTypes()[0] == DataOutput.class) {
                        a = k;
                        break;
                    }
                }
                if (a == null)
                    throw new CoreException("Failed to find NBTBase required method!");
                a.setAccessible(true);
                a.invoke(list, dos);
            }
            baos.flush();
            dos.flush();
            ret = new BigInteger(1, baos.toByteArray()).toString(32);
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to convert the ItemStack Array into a string.");
        }
        finally {
            FileUtils.closeQuietly(baos);
            FileUtils.closeQuietly(dos);
        }
        return encode(ret);
    }
    
    public static ItemStack getItemStackFromString(final String string) throws CoreException {
        ItemStack ret = null;
        if ("".equals(string) || string.isEmpty()) {
            return new ItemStack(Material.AIR);
        }
        if (string.equals("0")) {
            return new ItemStack(Material.AIR);
        }
        if (NumberUtils.isInteger(string)) {
            return new ItemStack(NumberUtils.toInteger(string), 1);
        }
        final Material material = Material.getMaterial(string);
        if (material != null) {
            return new ItemStack(Material.getMaterial(string), 1);
        }
        if (InventoryStack.stringItem.matcher(string).matches() && string.contains(":")) {
            final ItemStack stack = new ItemStack(Material.AIR, 1);
            final String[] split = string.split(":");
            if (NumberUtils.isInteger(split[0])) {
                stack.setData(new MaterialData(NumberUtils.toInteger(split[0]), (byte)NumberUtils.toInteger(split[1])));
            }
            else {
                stack.setData(new MaterialData(Material.getMaterial(split[0]), (byte)NumberUtils.toInteger(split[1])));
            }
        }
        ByteArrayInputStream bais = null;
        DataInputStream dis = null;
        try {
            bais = new ByteArrayInputStream(new BigInteger(decode(string), 32).toByteArray());
            dis = new DataInputStream(bais);
            final Class<?> baseCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "NBTBase");
            Method a = null;
            for (final Method m : baseCLS.getDeclaredMethods()) {
                if (m.getParameterTypes().length == 1 && !Modifier.isAbstract(m.getModifiers()) && m.getParameterTypes()[0] == DataInput.class) {
                    a = m;
                    break;
                }
            }
            if (a == null) {
                throw new CoreException("Failed to find NBTBase required method!");
            }
            Object compound = a.invoke(null, dis);
            compound = convert(compound, a.getReturnType());
            final boolean isEmpty = (boolean)compound.getClass().getMethod("isEmpty", (Class<?>[])new Class[0]).invoke(compound, new Object[0]);
            if (!isEmpty) {
                final Class<?> itemStackCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "ItemStack");
                final Object itemStack = itemStackCLS.getMethod("createStack", compound.getClass()).invoke(null, compound);
                final Class<?> craftItemStack = Class.forName("org.bukkit.craftbukkit." + InternalManager.getServerVersion() + "inventory.CraftItemStack");
                final Method asCopy = craftItemStack.getMethod("asBukkitCopy", itemStack.getClass());
                ret = (ItemStack)asCopy.invoke(null, itemStack);
            }
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to convert the String '" + string + "' into an ItemStack.");
        }
        finally {
            FileUtils.closeQuietly(bais);
            FileUtils.closeQuietly(dis);
        }
        return ret;
    }
    
    public static ItemStack[] getArrayFromString(final String string) throws CoreException {
        ItemStack[] ret = null;
        ByteArrayInputStream bais = null;
        DataInputStream dis = null;
        if ("".equals(string) || string.isEmpty()) {
            return new ItemStack[] { new ItemStack(Material.AIR) };
        }
        try {
            bais = new ByteArrayInputStream(new BigInteger(decode(string), 32).toByteArray());
            dis = new DataInputStream(bais);
            final Class<?> baseCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "NBTBase");
            Method a = null;
            for (final Method m : baseCLS.getDeclaredMethods()) {
                if (m.getParameterTypes().length == 1 && !Modifier.isAbstract(m.getModifiers()) && m.getParameterTypes()[0] == DataInput.class) {
                    a = m;
                    break;
                }
            }
            Object nbtlist;
            final Class<?> listCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "NBTTagList");
            if (a != null) {
                nbtlist = convert(a.invoke(null, dis), a.getReturnType());
            } else {
                for (final Method m : listCLS.getDeclaredMethods()) {
                    if (m.getParameterTypes().length == 2 && m.getParameterTypes()[0] == DataInput.class) {
                        a = m;
                        break;
                    }
                }
                if (a != null) {
                    nbtlist = listCLS.newInstance();
                    a.setAccessible(true);
                    a.invoke(nbtlist, dis, 0);
                } else {
                    for (final Method m : listCLS.getDeclaredMethods()) {
                        if (m.getParameterTypes().length == 3 && m.getParameterTypes()[0] == DataInput.class) {
                            a = m;
                            break;
                        }
                    }
                    if (a == null)
                        throw new CoreException("Failed to find NBTBase required method!");
                    final Class<?> unlimitedCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "NBTReadLimiterUnlimited");
                    Constructor unlimitedCons = unlimitedCLS.getDeclaredConstructors()[0];
                    unlimitedCons.setAccessible(true);
                    Object limiter = unlimitedCons.newInstance(1024);
                    nbtlist = listCLS.newInstance();
                    a.setAccessible(true);
                    a.invoke(nbtlist, dis, 0, limiter);
                }
            }
            final int size = (int)nbtlist.getClass().getMethod("size", (Class<?>[])new Class[0]).invoke(nbtlist, new Object[0]);
            ret = new ItemStack[size];
            final Class<?> compoundCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "NBTTagCompound");
            final Method get = nbtlist.getClass().getMethod("get", Integer.TYPE);
            final Class<?> itemStackCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "ItemStack");
            final Class<?> craftItemStack = Class.forName("org.bukkit.craftbukkit." + InternalManager.getServerVersion() + "inventory.CraftItemStack");
            for (int i = 0; i < size; ++i) {
                Object compound = get.invoke(nbtlist, i);
                compound = convert(compound, compoundCLS);
                final boolean isEmpty = (boolean)compound.getClass().getMethod("isEmpty", (Class<?>[])new Class[0]).invoke(compound, new Object[0]);
                if (!isEmpty) {
                    final Object itemStack = itemStackCLS.getMethod("createStack", compound.getClass()).invoke(null, compound);
                    final Method asCopy = craftItemStack.getMethod("asBukkitCopy", itemStack.getClass());
                    ret[i] = (ItemStack)asCopy.invoke(null, itemStack);
                }
            }
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to convert the String '" + string + "' into an ItemStack Array.");
        }
        finally {
            FileUtils.closeQuietly(bais);
            FileUtils.closeQuietly(dis);
        }
        return ret;
    }
    
    public static Object getCraftVersion(final ItemStack stack) {
        if (stack != null) {
            try {
                final Class<?> cls = Class.forName("org.bukkit.craftbukkit." + InternalManager.getServerVersion() + "inventory.CraftItemStack");
                final Method method = cls.getMethod("asNMSCopy", ItemStack.class);
                method.setAccessible(true);
                return method.invoke(null, stack);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    
    public static Object convert(final Object obj, final Class<?> type) {
        return type.cast(obj);
    }
    
    public static String encode(final String string) {
        if (string == null) {
            return "MA==";
        }
        return Base64Coder.encodeString(string);
    }
    
    public static String decode(final String string) {
        if (string == null) {
            return "";
        }
        return Base64Coder.decodeString(string);
    }
    
    static {
        InventoryStack.stringItem = Pattern.compile("([0-9]*|[a-zA-Z_]*):([0-9]*)");
    }
}
