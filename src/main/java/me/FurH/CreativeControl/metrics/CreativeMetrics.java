package me.FurH.CreativeControl.metrics;

import org.bukkit.configuration.file.*;
import org.bukkit.*;
import java.util.logging.*;
import org.bukkit.configuration.*;
import org.bukkit.plugin.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class CreativeMetrics
{
    private static final int REVISION = 5;
    private static final String BASE_URL = "http://mcstats.org";
    private static final String REPORT_URL = "/report/%s";
    private static final String CONFIG_FILE = "plugins/PluginMetrics/config.yml";
    private static final String CUSTOM_DATA_SEPARATOR = "~~";
    private static final int PING_INTERVAL = 10;
    private final Plugin plugin;
    private final Set<Graph> graphs;
    private final Graph defaultGraph;
    private final YamlConfiguration configuration;
    private final File configurationFile;
    private final String guid;
    private final Object optOutLock;
    private volatile int taskId;
    
    public CreativeMetrics(final Plugin plugin) throws IOException {
        super();
        this.graphs = Collections.synchronizedSet(new HashSet<Graph>());
        this.defaultGraph = new Graph("Default");
        this.optOutLock = new Object();
        this.taskId = -1;
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
        this.configurationFile = new File("plugins/PluginMetrics/config.yml");
        (this.configuration = YamlConfiguration.loadConfiguration(this.configurationFile)).addDefault("opt-out", (Object)false);
        this.configuration.addDefault("guid", (Object)UUID.randomUUID().toString());
        if (this.configuration.get("guid", (Object)null) == null) {
            this.configuration.options().header("http://mcstats.org").copyDefaults(true);
            this.configuration.save(this.configurationFile);
        }
        this.guid = this.configuration.getString("guid");
    }
    
    public Graph createGraph(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Graph name cannot be null");
        }
        final Graph graph = new Graph(name);
        this.graphs.add(graph);
        return graph;
    }
    
    public void addGraph(final Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        this.graphs.add(graph);
    }
    
    public void addCustomData(final Plotter plotter) {
        if (plotter == null) {
            throw new IllegalArgumentException("Plotter cannot be null");
        }
        this.defaultGraph.addPlotter(plotter);
        this.graphs.add(this.defaultGraph);
    }
    
    public boolean start() {
        synchronized (this.optOutLock) {
            if (this.isOptOut()) {
                return false;
            }
            if (this.taskId >= 0) {
                return true;
            }
            this.taskId = this.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(this.plugin, (Runnable)new Runnable() {
                private boolean firstPost = true;
                
                @Override
                public void run() {
                    try {
                        synchronized (CreativeMetrics.this.optOutLock) {
                            if (CreativeMetrics.this.isOptOut() && CreativeMetrics.this.taskId > 0) {
                                CreativeMetrics.this.plugin.getServer().getScheduler().cancelTask(CreativeMetrics.this.taskId);
                                CreativeMetrics.this.taskId = -1;
                                for (final Graph graph : CreativeMetrics.this.graphs) {
                                    graph.onOptOut();
                                }
                            }
                        }
                        CreativeMetrics.this.postPlugin(!this.firstPost);
                        this.firstPost = false;
                    }
                    catch (IOException e) {
                        Bukkit.getLogger().log(Level.INFO, "[Metrics] " + e.getMessage());
                    }
                }
            }, 0L, 12000L);
            return true;
        }
    }
    
    public boolean isOptOut() {
        synchronized (this.optOutLock) {
            try {
                this.configuration.load("plugins/PluginMetrics/config.yml");
            }
            catch (IOException ex) {
                Bukkit.getLogger().log(Level.INFO, "[Metrics] " + ex.getMessage());
                return true;
            }
            catch (InvalidConfigurationException ex2) {
                Bukkit.getLogger().log(Level.INFO, "[Metrics] " + ex2.getMessage());
                return true;
            }
            return this.configuration.getBoolean("opt-out", false);
        }
    }
    
    public void enable() throws IOException {
        synchronized (this.optOutLock) {
            if (this.isOptOut()) {
                this.configuration.set("opt-out", (Object)false);
                this.configuration.save(this.configurationFile);
            }
            if (this.taskId < 0) {
                this.start();
            }
        }
    }
    
    public void disable() throws IOException {
        synchronized (this.optOutLock) {
            if (!this.isOptOut()) {
                this.configuration.set("opt-out", (Object)true);
                this.configuration.save(this.configurationFile);
            }
            if (this.taskId > 0) {
                this.plugin.getServer().getScheduler().cancelTask(this.taskId);
                this.taskId = -1;
            }
        }
    }
    
    private void postPlugin(final boolean isPing) throws IOException {
        final PluginDescriptionFile description = this.plugin.getDescription();
        final StringBuilder data = new StringBuilder();
        data.append(encode("guid")).append('=').append(encode(this.guid));
        encodeDataPair(data, "version", description.getVersion());
        encodeDataPair(data, "server", Bukkit.getVersion());
        encodeDataPair(data, "players", Integer.toString(Bukkit.getServer().getOnlinePlayers().size()));
        encodeDataPair(data, "revision", String.valueOf(5));
        if (isPing) {
            encodeDataPair(data, "ping", "true");
        }
        synchronized (this.graphs) {
            for (final Graph graph : this.graphs) {
                for (final Plotter plotter : graph.getPlotters()) {
                    final String key = String.format("C%s%s%s%s", "~~", graph.getName(), "~~", plotter.getColumnName());
                    final String value = Integer.toString(plotter.getValue());
                    encodeDataPair(data, key, value);
                }
            }
        }
        final URL url = new URL("http://mcstats.org" + String.format("/report/%s", encode(this.plugin.getDescription().getName())));
        URLConnection connection;
        if (this.isMineshafterPresent()) {
            connection = url.openConnection(Proxy.NO_PROXY);
        }
        else {
            connection = url.openConnection();
        }
        connection.setDoOutput(true);
        final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(data.toString());
        writer.flush();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final String response = reader.readLine();
        writer.close();
        reader.close();
        if (response == null || response.startsWith("ERR")) {
            throw new IOException(response);
        }
        if (response.contains("OK This is your first update this hour")) {
            synchronized (this.graphs) {
                for (final Graph graph2 : this.graphs) {
                    for (final Plotter plotter2 : graph2.getPlotters()) {
                        plotter2.reset();
                    }
                }
            }
        }
    }
    
    private boolean isMineshafterPresent() {
        try {
            Class.forName("mineshafter.MineServer");
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    private static void encodeDataPair(final StringBuilder buffer, final String key, final String value) throws UnsupportedEncodingException {
        buffer.append('&').append(encode(key)).append('=').append(encode(value));
    }
    
    private static String encode(final String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }
    
    public static class Graph
    {
        private final String name;
        private final Set<Plotter> plotters;
        
        private Graph(final String name) {
            super();
            this.plotters = new LinkedHashSet<Plotter>();
            this.name = name;
        }
        
        public String getName() {
            return this.name;
        }
        
        public void addPlotter(final Plotter plotter) {
            this.plotters.add(plotter);
        }
        
        public void removePlotter(final Plotter plotter) {
            this.plotters.remove(plotter);
        }
        
        public Set<Plotter> getPlotters() {
            return Collections.unmodifiableSet((Set<? extends Plotter>)this.plotters);
        }
        
        @Override
        public int hashCode() {
            return this.name.hashCode();
        }
        
        @Override
        public boolean equals(final Object object) {
            if (!(object instanceof Graph)) {
                return false;
            }
            final Graph graph = (Graph)object;
            return graph.name.equals(this.name);
        }
        
        protected void onOptOut() {
        }
    }
    
    public abstract static class Plotter
    {
        private final String name;
        
        public Plotter() {
            this("Default");
        }
        
        public Plotter(final String name) {
            super();
            this.name = name;
        }
        
        public abstract int getValue();
        
        public String getColumnName() {
            return this.name;
        }
        
        public void reset() {
        }
        
        @Override
        public int hashCode() {
            return this.getColumnName().hashCode();
        }
        
        @Override
        public boolean equals(final Object object) {
            if (!(object instanceof Plotter)) {
                return false;
            }
            final Plotter plotter = (Plotter)object;
            return plotter.name.equals(this.name) && plotter.getValue() == this.getValue();
        }
    }
}
