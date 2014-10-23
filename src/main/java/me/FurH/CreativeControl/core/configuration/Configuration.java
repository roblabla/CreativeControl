package me.FurH.CreativeControl.core.configuration;

import me.FurH.CreativeControl.core.cache.*;
import org.bukkit.configuration.file.*;
import me.FurH.CreativeControl.core.*;
import org.bukkit.configuration.*;
import me.FurH.CreativeControl.core.util.*;
import org.bukkit.*;
import me.FurH.CreativeControl.core.list.*;
import me.FurH.CreativeControl.core.exceptions.*;
import java.util.*;
import java.io.*;
import me.FurH.CreativeControl.core.file.*;

public class Configuration
{
    private CoreSafeCache<String, YamlConfiguration> yamlcache;
    private String default_setting;
    private String default_world;
    private String default_message;
    private HashSet<String> update_required;
    private boolean single_config;
    private YamlConfiguration settings;
    private YamlConfiguration messages;
    protected CorePlugin plugin;
    private ConfigUpdater updater;
    
    public Configuration(final CorePlugin plugin) {
        super();
        this.yamlcache = new CoreSafeCache<String, YamlConfiguration>();
        this.default_setting = "settings.yml";
        this.default_world = "world.yml";
        this.default_message = "messages.yml";
        this.update_required = new HashSet<String>();
        this.single_config = false;
        this.plugin = plugin;
    }
    
    public void setSingleConfig(final boolean single_config1) {
        this.single_config = single_config1;
    }
    
    public void updateConfig() {
        for (final String dir : this.update_required) {
            final File file = new File(dir);
            this.updateLines(file, this.getInputStream(file));
        }
        this.update_required.clear();
        this.unload();
    }
    
    public void unload() {
        this.yamlcache.clear();
        this.settings = null;
        this.messages = null;
    }
    
    private YamlConfiguration config(final File file) {
        final Communicator com = this.plugin.getCommunicator();
        YamlConfiguration yaml = null;
        try {
            if (file.getName().equals(this.default_setting)) {
                if (this.settings == null) {
                    (this.settings = new YamlConfiguration()).load(file);
                }
                return this.settings;
            }
            if (file.getName().equals(this.default_message)) {
                if (this.messages == null) {
                    (this.messages = new YamlConfiguration()).load(file);
                }
                return this.messages;
            }
            if (this.yamlcache.containsKey(file.getName())) {
                return this.yamlcache.get(file.getName());
            }
            yaml = new YamlConfiguration();
            yaml.load(file);
        }
        catch (FileNotFoundException ex) {
            com.error(ex, "The file " + file.getName() + "  was not found in the plugin directory!", new Object[0]);
        }
        catch (IOException ex2) {
            com.error(ex2, "Failed to load the " + file.getName() + " configuration file!", new Object[0]);
        }
        catch (InvalidConfigurationException ex3) {
            com.log("[TAG] You have a broken node in your " + file.getName() + " file, use http://yaml-online-parser.appspot.com/ to find errors! " + ex3.getMessage(), new Object[0]);
            this.update_required.add(file.getAbsolutePath());
        }
        if (yaml != null) {
            this.yamlcache.put(file.getName(), yaml);
        }
        return yaml;
    }
    
    protected boolean getBoolean(final World w, final String node) {
        final Object object = this.get(this.getWorldFile(w), node);
        return object instanceof Boolean && (boolean)object;
    }
    
    protected boolean getBoolean(final String node) {
        final Object object = this.get(this.getSettingsFile(), node);
        return object instanceof Boolean && (boolean)object;
    }
    
    protected int getInteger(final World w, final String node) {
        final Object object = this.get(this.getWorldFile(w), node);
        return (object instanceof Number) ? ((Number)object).intValue() : 0;
    }
    
    protected int getInteger(final String node) {
        final Object object = this.get(this.getSettingsFile(), node);
        return (object instanceof Number) ? ((Number)object).intValue() : 0;
    }
    
    protected double getDouble(final World w, final String node) {
        final Object object = this.get(this.getWorldFile(w), node);
        return (object instanceof Number) ? ((Number)object).doubleValue() : 0.0;
    }
    
    protected double getDouble(final String node) {
        final Object object = this.get(this.getSettingsFile(), node);
        return (object instanceof Number) ? ((Number)object).doubleValue() : 0.0;
    }
    
    public String getString(final String node) {
        final Object object = this.get(this.getSettingsFile(), node);
        try {
            return new String(object.toString().getBytes(), "UTF8");
        }
        catch (UnsupportedEncodingException ex) {
            return object.toString();
        }
    }
    
    public String getString(final World w, final String node) {
        final Object object = this.get(this.getWorldFile(w), node);
        try {
            return new String(object.toString().getBytes(), "UTF8");
        }
        catch (UnsupportedEncodingException ex) {
            return object.toString();
        }
    }
    
    protected long getLong(final String node) {
        final Object object = this.get(this.getSettingsFile(), node);
        return (object instanceof Number) ? ((Number)object).longValue() : 0L;
    }
    
    protected long getLong(final World w, final String node) {
        final Object object = this.get(this.getWorldFile(w), node);
        return (object instanceof Number) ? ((Number)object).longValue() : 0L;
    }
    
    protected HashSet<String> getStringAsStringSet(final World w, final String node) {
        try {
            return CollectionUtils.toStringHashSet(this.getString(w, node).replaceAll(" ", ""), ",");
        }
        catch (CoreException ex) {
            this.plugin.getCommunicator().error(ex);
            return null;
        }
    }
    
    protected HashSet<String> getStringAsStringSet(final String node) {
        try {
            return CollectionUtils.toStringHashSet(this.getString(node).replaceAll(" ", ""), ",");
        }
        catch (CoreException ex) {
            this.plugin.getCommunicator().error(ex);
            return null;
        }
    }
    
    protected HashSet<Integer> getStringAsIntegerSet(final World w, final String node) {
        try {
            return CollectionUtils.toIntegerHashSet(this.getString(w, node).replaceAll(" ", ""), ",");
        }
        catch (CoreException ex) {
            this.plugin.getCommunicator().error(ex);
            return null;
        }
    }
    
    protected HashSet<Integer> getStringAsIntegerSet(final String node) {
        try {
            return CollectionUtils.toIntegerHashSet(this.getString(node).replaceAll(" ", ""), ",");
        }
        catch (CoreException ex) {
            this.plugin.getCommunicator().error(ex);
            return null;
        }
    }
    
    protected List<Integer> getStringAsIntegerList(final World w, final String node) {
        try {
            return CollectionUtils.toIntegerList(this.getString(w, node).replaceAll(" ", ""), ",");
        }
        catch (CoreException ex) {
            this.plugin.getCommunicator().error(ex);
            return null;
        }
    }
    
    protected List<Integer> getStringAsIntegerList(final String node) {
        try {
            return CollectionUtils.toIntegerList(this.getString(node).replaceAll(" ", ""), ",");
        }
        catch (CoreException ex) {
            this.plugin.getCommunicator().error(ex);
            return null;
        }
    }
    
    public String getMessage(final String node) {
        final Object object = this.get(this.getMessagesFile(), node);
        try {
            return new String(object.toString().getBytes(), "UTF8");
        }
        catch (UnsupportedEncodingException ex) {
            return object.toString();
        }
    }
    
    public List<String> getStringList(final String node) {
        return CollectionUtils.getStringList(this.get(this.getSettingsFile(), node));
    }
    
    public List<String> getStringList(final World w, final String node) {
        return CollectionUtils.getStringList(this.get(this.getWorldFile(w), node));
    }
    
    public List<Integer> getIntegerList(final String node) {
        return CollectionUtils.getIntegerList(this.get(this.getSettingsFile(), node));
    }
    
    public List<Integer> getIntegerList(final World w, final String node) {
        return CollectionUtils.getIntegerList(this.get(this.getWorldFile(w), node));
    }
    
    public boolean hasNode(final World w, final String node) {
        return this.hasNode(this.getWorldFile(w), node);
    }
    
    public boolean hasNode(final String node) {
        return this.hasNode(this.getSettingsFile(), node);
    }
    
    public boolean hasNode(final File file, final String node) {
        return this.config(file).contains(node);
    }
    
    public void set(final File file, final String node, final Object value) {
        final Communicator com = this.plugin.getCommunicator();
        final YamlConfiguration config = this.config(file);
        config.set(node, value);
        try {
            config.save(file);
        }
        catch (IOException ex) {
            com.error(ex, "Failed to update the '" + node + ":" + value + "' " + file.getName() + " node.", new Object[0]);
        }
        this.update_required.add(file.getAbsolutePath());
    }
    
    public Object get(final File file, final String node) {
        final Communicator com = this.plugin.getCommunicator();
        Object backup = null;
        try {
            if (!this.config(file).contains(node)) {
                final YamlConfiguration rsconfig = new YamlConfiguration();
                rsconfig.load(this.getInputStream(file));
                if (rsconfig.contains(node)) {
                    com.log("[TAG] Settings file updated, check at: \n" + node.replace(".", " &3>>&f "), new Object[0]);
                    backup = rsconfig.get(node);
                    this.update_required.add(file.getAbsolutePath());
                }
                else {
                    com.log("[TAG] Invalid node at: " + node + ", contact the developer!", new Object[0]);
                }
            }
        }
        catch (IOException ex) {
            com.error(ex, "Can't load the " + file.getName() + " file: " + ex.getMessage() + ", node " + node, new Object[0]);
        }
        catch (InvalidConfigurationException ex2) {
            com.log("[TAG] You have a broken node in your " + file.getName() + " file, use http://yaml-online-parser.appspot.com/ to find errors! " + ex2.getMessage(), new Object[0]);
            this.update_required.add(file.getAbsolutePath());
        }
        Object value = this.config(file).get(node);
        if (value == null) {
            if (backup != null) {
                value = backup;
            }
            else {
                value = "0";
                com.log("[TAG] Can't get " + file.getName() + " node: " + node + ", contact the developer.", new Object[0]);
                this.update_required.add(file.getAbsolutePath());
            }
        }
        return value;
    }
    
    private InputStream getInputStream(final File file) {
        String source = this.default_setting;
        if (file.getName().equals(this.default_setting)) {
            source = this.default_setting;
        }
        else if (file.getName().equals(this.default_message)) {
            source = this.default_message;
        }
        else {
            source = this.default_world;
        }
        return this.plugin.getResource(source);
    }
    
    public File getWorldFile(final World w) {
        final File file = new File(this.plugin.getDataFolder() + File.separator + "worlds", (w != null || this.single_config) ? (w.getName() + ".yml") : this.default_world);
        if (!file.exists()) {
            try {
                FileUtils.copyFile(this.plugin.getResource(this.default_world), file);
            }
            catch (CoreException ex) {
                this.plugin.getCommunicator().error(ex);
            }
            this.updateLines(file, this.getInputStream(file));
        }
        return file;
    }
    
    public File getMessagesFile() {
        final File file = new File(this.plugin.getDataFolder(), this.default_message);
        if (!file.exists()) {
            try {
                FileUtils.copyFile(this.plugin.getResource(this.default_message), file);
            }
            catch (CoreException ex) {
                this.plugin.getCommunicator().error(ex);
            }
            this.updateLines(file, this.getInputStream(file));
        }
        return file;
    }
    
    public File getSettingsFile() {
        final File file = new File(this.plugin.getDataFolder(), this.default_setting);
        if (!file.exists()) {
            try {
                FileUtils.copyFile(this.plugin.getResource(this.default_setting), file);
            }
            catch (CoreException ex) {
                this.plugin.getCommunicator().error(ex);
            }
            this.updateLines(file, this.getInputStream(file));
        }
        return file;
    }
    
    private void updateLines(final File file, final InputStream stream) {
        if (this.updater == null) {
            this.updater = new ConfigUpdater();
        }
        this.updater.updateLines(file, stream);
    }
}
