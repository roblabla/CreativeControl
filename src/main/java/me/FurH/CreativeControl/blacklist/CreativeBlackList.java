package me.FurH.CreativeControl.blacklist;

import me.FurH.CreativeControl.stack.*;
import java.util.*;
import me.FurH.CreativeControl.*;
import me.FurH.CreativeControl.core.util.*;

public class CreativeBlackList
{
    private static CreativeItemStack fixed;
    
    public boolean isBlackListed(final HashSet<CreativeItemStack> source, final CreativeItemStack check) {
        if (source.contains(CreativeBlackList.fixed)) {
            return true;
        }
        if (source.contains(check)) {
            return true;
        }
        for (final CreativeItemStack item : source) {
            if (item.equals(check)) {
                source.add(check);
                return true;
            }
        }
        return false;
    }
    
    public HashSet<CreativeItemStack> buildHashSet(final HashSet<String> source) {
        final HashSet<CreativeItemStack> ret = new HashSet<CreativeItemStack>();
        for (final String string : source) {
            if (!this.isClearInteger(string)) {
                continue;
            }
            if (!string.contains(":")) {
                ret.add(new CreativeItemStack(Integer.parseInt(string), -1));
            }
            else {
                ret.add(new CreativeItemStack(Integer.parseInt(string.split(":")[0]), Integer.parseInt(string.split(":")[1])));
            }
        }
        return ret;
    }
    
    private boolean isClearInteger(final String string) {
        final Communicator com = CreativeControl.getPlugin().getCommunicator();
        try {
            Integer.parseInt(string.replaceAll("[^0-9-.]", ""));
        }
        catch (Exception ex) {
            com.log("[TAG] &c" + string + " is not a valid item id!", new Object[0]);
            return false;
        }
        return true;
    }
    
    static {
        CreativeBlackList.fixed = new CreativeItemStack(-59941, -1);
    }
}
