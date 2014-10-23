package me.FurH.CreativeControl.core.inventory;

import java.util.regex.*;
import org.bukkit.inventory.*;
import me.FurH.CreativeControl.core.number.*;
import org.bukkit.enchantments.*;
import java.util.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.*;

@Deprecated
public class InvUtils
{
    private static Pattern itemStack1;
    private static Pattern itemStack2;
    
    public static ItemStack[] toArrayStack(final List<String> stacks) {
        return toArrayStack(stacks.toArray(new String[0]));
    }
    
    public static ItemStack[] toArrayStack(final String item) {
        return toArrayStack(item.substring(1, item.length() - 1).split(", "));
    }
    
    public static ItemStack[] toArrayStack(final String[] stacks) {
        final ItemStack[] items = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; ++i) {
            items[i] = stringToItemStack(stacks[i]);
        }
        return items;
    }
    
    public static ItemStack stringToItemStack(final String string) {
        ItemStack stack = new ItemStack(Material.AIR);
        try {
            if (string.equals("0")) {
                return null;
            }
            if (NumberUtils.isInteger(string)) {
                return new ItemStack(NumberUtils.toInteger(string), 1);
            }
            if (Material.getMaterial(string) != null) {
                return new ItemStack(Material.getMaterial(string), 1);
            }
            if (InvUtils.itemStack1.matcher(string).matches()) {
                return new ItemStack(NumberUtils.toInteger(string.split(":")[0]), 1, (short)0, Byte.parseByte(string.split(":")[1]));
            }
            if (InvUtils.itemStack2.matcher(string).matches()) {
                return new ItemStack(Material.getMaterial(string.split(":")[0]), 1, (short)0, Byte.parseByte(string.split(":")[1]));
            }
            if (string.equals("[]")) {
                return stack;
            }
            if (!string.contains(":")) {
                return stack;
            }
            final String[] inv = string.split(":");
            if (inv.length < 4) {
                return stack;
            }
            final String id = inv[0];
            final String data = inv[1];
            final String amount = inv[2];
            final String durability = inv[3];
            final String enchantments = inv[4];
            boolean meta = false;
            try {
                final String fire = inv[5];
                meta = true;
            }
            catch (Exception ex) {
                meta = false;
            }
            if (!string.equals("0:0:0:1:[]")) {
                try {
                    stack = new ItemStack(Integer.parseInt(id));
                    stack.setAmount(Integer.parseInt(amount));
                    stack.setDurability(Short.parseShort(durability));
                    int i = Integer.parseInt(data);
                    if (i > 128) {
                        i = 128;
                    }
                    stack.getData().setData((byte)i);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
                final Map<Enchantment, Integer> enchants = getEnchantments(enchantments);
                if (stack.getType() == Material.ENCHANTED_BOOK) {
                    final EnchantmentStorageMeta enchant = (EnchantmentStorageMeta)stack.getItemMeta();
                    for (final Enchantment e : enchants.keySet()) {
                        enchant.addStoredEnchant(e, (int)enchants.get(e), true);
                    }
                    stack.setItemMeta((ItemMeta)enchant);
                }
                else {
                    stack.addUnsafeEnchantments((Map)enchants);
                }
                if (meta) {
                    stack = setItemMeta(stack, inv[5]);
                    if (stack.getType() == Material.FIREWORK) {
                        stack = getFireWork(stack, string);
                    }
                    if (stack.getType() == Material.BOOK || stack.getType() == Material.BOOK_AND_QUILL || stack.getType() == Material.WRITTEN_BOOK) {
                        stack = setBookMeta(stack, inv[5]);
                    }
                    if (stack.getType() == Material.LEATHER_HELMET || stack.getType() == Material.LEATHER_CHESTPLATE || stack.getType() == Material.LEATHER_LEGGINGS || stack.getType() == Material.LEATHER_BOOTS) {
                        stack = setArmorMeta(stack, inv[5]);
                    }
                }
            }
        }
        catch (Exception ex2) {
            ex2.printStackTrace();
        }
        return stack;
    }
    
    private static Map<Enchantment, Integer> getEnchantments(String enchantments) {
        final Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
        if (!enchantments.equals("[]")) {
            enchantments = enchantments.replaceAll("[^a-zA-Z0-9_:,=]", "");
            final String[] enchant = enchantments.split(",");
            final List<String> encht = new ArrayList<String>();
            encht.addAll(Arrays.asList(enchant));
            for (final String exlvl : encht) {
                if (exlvl.contains("=")) {
                    final String[] split = exlvl.split("=");
                    final String name = split[0];
                    final String lvl = split[1];
                    try {
                        final Enchantment ext = Enchantment.getByName(name);
                        enchants.put(ext, Integer.parseInt(lvl));
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return enchants;
    }
    
    public static String toListString(final ItemStack[] source) {
        return toStackStringList(source).toString();
    }
    
    public static List<String> toStackStringList(final ItemStack[] source) {
        final List<String> items = new ArrayList<String>();
        for (final ItemStack item : source) {
            items.add(itemStackToString(item));
        }
        return items;
    }
    
    public static String itemStackToString(final ItemStack item) {
        String ret = "0";
        try {
            if (item == null) {
                return "0";
            }
            if (item.getType() == Material.AIR) {
                return "0";
            }
            final int type = item.getTypeId();
            final int amount = item.getAmount();
            final byte data = item.getData().getData();
            final short durability = item.getDurability();
            Map<Enchantment, Integer> e1 = (Map<Enchantment, Integer>)item.getEnchantments();
            if (item.getType() == Material.ENCHANTED_BOOK) {
                final EnchantmentStorageMeta enchant = (EnchantmentStorageMeta)item.getItemMeta();
                e1 = (Map<Enchantment, Integer>)enchant.getStoredEnchants();
            }
            final List<String> enchantments = new ArrayList<String>();
            for (final Enchantment key : e1.keySet()) {
                enchantments.add(key.getName() + "=" + e1.get(key));
            }
            ret = ("'" + type + ":" + data + ":" + amount + ":" + durability + ":" + enchantments + "'").replaceAll("[^a-zA-Z0-9:,_=\\[\\]]", "");
            if (item.hasItemMeta() && item.getType() == Material.FIREWORK) {
                ret += getFireWork(item);
            }
            if ((item.hasItemMeta() && item.getType() == Material.BOOK) || item.getType() == Material.BOOK_AND_QUILL || item.getType() == Material.WRITTEN_BOOK) {
                ret += getBookMeta(item);
            }
            if ((item.hasItemMeta() && item.getType() == Material.LEATHER_HELMET) || item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.LEATHER_LEGGINGS || item.getType() == Material.LEATHER_BOOTS) {
                ret += getArmorMeta(item);
            }
            if (item.hasItemMeta()) {
                ret += getItemMeta(item);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }
    
    public static String getArmorMeta(final ItemStack stack) {
        return ":" + getColor(((LeatherArmorMeta)stack.getItemMeta()).getColor());
    }
    
    public static ItemStack setArmorMeta(final ItemStack stack, final String string) {
        final LeatherArmorMeta meta = (LeatherArmorMeta)stack.getItemMeta();
        try {
            meta.setColor(getColor(Integer.parseInt(string)));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        stack.setItemMeta((ItemMeta)meta);
        return stack;
    }
    
    public static String getBookMeta(final ItemStack stack) {
        final BookMeta meta = (BookMeta)stack.getItemMeta();
        final int hasTitle = meta.hasTitle() ? 1 : 0;
        final String title = encode(meta.getTitle());
        final int hasAuthor = meta.hasAuthor() ? 1 : 0;
        final String author = encode(meta.getAuthor());
        final int hasPages = meta.hasPages() ? 1 : 0;
        String pages = "";
        for (final String page : meta.getPages()) {
            pages = pages + encode(page) + "!";
        }
        return ":" + hasTitle + ";" + title + ";" + hasAuthor + ";" + author + ";" + hasPages + ";" + pages.substring(0, pages.length());
    }
    
    public static ItemStack setBookMeta(final ItemStack stack, final String string) {
        final BookMeta meta = (BookMeta)stack.getItemMeta();
        try {
            final String[] split = string.split(";");
            if (split.length < 5) {
                return stack;
            }
            final boolean hasTitle = "1".equals(split[0]);
            final String title = decode(split[1]);
            if (hasTitle) {
                meta.setTitle(title);
            }
            final boolean hasAuthor = "1".equals(split[2]);
            if (hasAuthor) {
                final String author = decode(split[3]);
                meta.setAuthor(author);
            }
            final boolean hasPages = "1".equals(split[4]);
            if (hasPages) {
                final List<String> pages = new ArrayList<String>();
                for (final String s : split[5].split("!")) {
                    pages.add(decode(s));
                }
                meta.setPages((List)pages);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        stack.setItemMeta((ItemMeta)meta);
        return stack;
    }
    
    public static String getFireWork(final ItemStack item) {
        final FireworkMeta meta = (FireworkMeta)item.getItemMeta();
        if (!meta.hasEffects()) {
            return "";
        }
        String effects = "";
        for (final FireworkEffect effect : meta.getEffects()) {
            String colors = "[";
            for (final Color color : effect.getColors()) {
                colors = colors + getColor(color) + "!";
            }
            if (!colors.equals("[")) {
                colors = colors.substring(0, colors.length() - 1);
            }
            colors += "]";
            String fadecolors = "[";
            for (final Color color2 : effect.getFadeColors()) {
                fadecolors = fadecolors + getColor(color2) + "!";
            }
            if (!fadecolors.equals("[")) {
                fadecolors = fadecolors.substring(0, fadecolors.length() - 1);
            }
            fadecolors += "]";
            final String ef = "{" + getType(effect.getType()) + ";" + colors + ";" + fadecolors + ";" + (effect.hasFlicker() ? "1" : "0") + ";" + (effect.hasTrail() ? "1" : "0") + "}. ";
            effects += ef;
        }
        effects = effects.substring(0, effects.length() - 2);
        return meta.getPower() + ":" + effects;
    }
    
    public static ItemStack getFireWork(final ItemStack stack, final String string) {
        final FireworkMeta meta = (FireworkMeta)stack.getItemMeta();
        try {
            final String[] inv = string.split(":");
            if (inv.length < 6) {
                return stack;
            }
            final int power = Integer.parseInt(inv[5]);
            List<String> effects = new ArrayList<String>();
            if (!inv[6].equals("[]")) {
                effects = Arrays.asList(inv[6].substring(1, inv[6].length() - 1).split("\\."));
            }
            for (final String effect : effects) {
                final FireworkEffect.Builder builder = FireworkEffect.builder();
                final String[] data = effect.split(";");
                if (data.length < 4) {
                    continue;
                }
                if (data[0].contains("{")) {
                    data[0] = data[0].substring(1);
                }
                builder.with(getType(Integer.parseInt(data[0])));
                for (final String color : Arrays.asList(data[1].substring(1, data[1].length() - 1).split("!"))) {
                    if (!color.isEmpty()) {
                        builder.withColor(getColor(Integer.parseInt(color)));
                    }
                }
                for (final String color : Arrays.asList(data[2].substring(1, data[2].length() - 1).split("!"))) {
                    if (!color.isEmpty()) {
                        builder.withFade(getColor(Integer.parseInt(color)));
                    }
                }
                builder.flicker(Integer.parseInt(data[3]) == 1);
                if (data[4].contains("}")) {
                    data[4] = data[4].substring(0, data[4].length() - 1);
                }
                builder.trail(Integer.parseInt(data[4]) == 1);
                meta.addEffect(builder.build());
            }
            meta.setPower(power);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        stack.setItemMeta((ItemMeta)meta);
        return stack;
    }
    
    public static FireworkEffect.Type getType(final int id) {
        switch (id) {
            case 0: {
                return FireworkEffect.Type.BALL;
            }
            case 1: {
                return FireworkEffect.Type.BALL_LARGE;
            }
            case 2: {
                return FireworkEffect.Type.BURST;
            }
            case 3: {
                return FireworkEffect.Type.CREEPER;
            }
            case 4: {
                return FireworkEffect.Type.STAR;
            }
            default: {
                return FireworkEffect.Type.BALL;
            }
        }
    }
    
    public static int getType(final FireworkEffect.Type type) {
        if (type == FireworkEffect.Type.BALL) {
            return 0;
        }
        if (type == FireworkEffect.Type.BALL_LARGE) {
            return 1;
        }
        if (type == FireworkEffect.Type.BURST) {
            return 2;
        }
        if (type == FireworkEffect.Type.CREEPER) {
            return 3;
        }
        if (type == FireworkEffect.Type.STAR) {
            return 4;
        }
        return 0;
    }
    
    public static int getColor(final Color color) {
        return color.asRGB();
    }
    
    public static Color getColor(final int rgb) {
        return Color.fromRGB(rgb);
    }
    
    public static String getItemMeta(final ItemStack stack) {
        final ItemMeta meta = stack.getItemMeta();
        final int hasDisplayName = meta.hasDisplayName() ? 1 : 0;
        final String displayName = encode(meta.getDisplayName());
        final int hasLore = meta.hasLore() ? 1 : 0;
        String lore = "";
        if (meta.hasLore()) {
            for (final String page : meta.getLore()) {
                lore = lore + encode(page) + "!";
            }
        }
        if (!lore.isEmpty()) {
            lore = lore.substring(0, lore.length());
        }
        return ":" + hasDisplayName + ";" + displayName + ";" + hasLore + ";" + lore;
    }
    
    public static ItemStack setItemMeta(final ItemStack stack, final String string) {
        final ItemMeta meta = stack.getItemMeta();
        try {
            final String[] split = string.split(";");
            if (split.length < 3) {
                return stack;
            }
            final boolean hasDisplayName = "1".equals(split[0]);
            final String displayName = decode(split[1]);
            if (hasDisplayName) {
                meta.setDisplayName(displayName);
            }
            final boolean hasLore = "1".equals(split[2]);
            if (hasLore) {
                final List<String> lore = new ArrayList<String>();
                for (final String s : split[3].split("!")) {
                    lore.add(decode(s));
                }
                meta.setLore((List)lore);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        stack.setItemMeta(meta);
        return stack;
    }
    
    public static String encode(final String string) {
        return InventoryStack.encode(string);
    }
    
    public static String decode(final String string) {
        return InventoryStack.decode(string);
    }
    
    static {
        InvUtils.itemStack1 = Pattern.compile("[0-9]+:[0-9]+");
        InvUtils.itemStack2 = Pattern.compile("[a-zA-Z_]+:[0-9]+");
    }
}
