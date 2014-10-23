package me.FurH.CreativeControl.core.database;

import me.FurH.CreativeControl.core.cache.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.core.util.*;
import java.io.*;
import java.sql.*;
import org.bukkit.*;
import org.bukkit.plugin.*;
import java.text.*;
import me.FurH.CreativeControl.core.*;
import java.util.*;

public class CoreSQLDatabase
{
    private CoreSafeCache<String, Statement> cache;
    private ConcurrentLinkedQueue<String> queue;
    private AtomicBoolean lock;
    private AtomicBoolean kill;
    public Connection connection;
    private boolean allow_mainthread;
    public String database_host;
    private String database_port;
    private String database_table;
    private String database_user;
    private String database_pass;
    public String prefix;
    private String engine;
    public DBType type;
    private CorePlugin plugin;
    public double queue_speed;
    public int queue_threads;
    private File database;
    public int version;
    private int writes;
    private int reads;
    private int fix;
    
    public CoreSQLDatabase(final CorePlugin plugin, final String prefix, final String engine, final String database_host, final String database_port, final String database_table, final String database_user, final String database_pass) {
        super();
        this.cache = new CoreSafeCache<String, Statement>();
        this.queue = new ConcurrentLinkedQueue<String>();
        this.lock = new AtomicBoolean(false);
        this.kill = new AtomicBoolean(false);
        this.allow_mainthread = true;
        this.database_host = "localhost";
        this.database_port = "3306";
        this.database_table = "minecraft";
        this.database_user = "root";
        this.database_pass = "123";
        this.prefix = "core_";
        this.engine = "SQLite";
        this.type = null;
        this.queue_speed = 0.1;
        this.queue_threads = 1;
        this.version = 1;
        this.writes = 0;
        this.reads = 0;
        this.fix = 0;
        this.database_host = database_host;
        this.database_port = database_port;
        this.database_table = database_table;
        this.database_pass = database_pass;
        this.database_user = database_user;
        this.plugin = plugin;
        this.prefix = prefix;
        this.engine = engine;
        plugin.coredatabase = this;
    }
    
    public DBType getDatabaseEngine() {
        return this.type;
    }
    
    public void setDatabaseVersion(final int version) {
        this.version = version;
    }
    
    public void setupQueue(final double queue_speed, final int queue_threads) {
        this.queue_speed = queue_speed;
        this.queue_threads = queue_threads;
    }
    
    public void setAllowMainThread(final boolean thread) {
        this.allow_mainthread = thread;
    }
    
    public long ping() throws CoreException {
        long ping = 0L;
        final DBType type = this.type;
        final DBType type2 = this.type;
        if (type == DBType.MySQL) {
            ping = Utils.pingServer(this.database_host + ":" + this.database_port);
        }
        return ping;
    }
    
    public String getAutoVariable(final DBType type) {
        if (type == DBType.MySQL || type == DBType.H2) {
            return "id INT AUTO_INCREMENT, PRIMARY KEY (id)";
        }
        return "id INTEGER PRIMARY KEY AUTOINCREMENT";
    }
    
    public int getQueueSize() {
        return this.queue.size();
    }
    
    public int getReads() {
        return this.reads;
    }
    
    public int getWrites() {
        return this.writes;
    }
    
    public long getTableCount(final String table) throws CoreException {
        long count = 0L;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final DBType type = this.type;
            final DBType type2 = this.type;
            if (type.equals(DBType.MySQL)) {
                ps = this.connection.prepareStatement("SELECT table_rows FROM information_schema.TABLES WHERE TABLE_NAME = '" + table + "' AND TABLE_SCHEMA = '" + this.database_table + "' LIMIT 1;");
                rs = ps.executeQuery();
                if (rs.next()) {
                    count += rs.getLong("table_rows");
                }
            }
            else {
                ps = this.connection.prepareStatement("SELECT COUNT(1) AS total FROM '" + table + "';");
                rs = ps.executeQuery();
                if (rs.next()) {
                    count += rs.getInt("total");
                }
            }
        }
        catch (SQLException ex) {
            throw new CoreException(ex, "Failed to count the table '" + table + "' rows");
        }
        finally {
            closeQuietly(ps);
            closeQuietly(rs);
        }
        return count;
    }
    
    public long getTableSize(final String table) throws CoreException {
        long size = 0L;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final DBType type = this.type;
            final DBType type2 = this.type;
            if (type.equals(DBType.MySQL)) {
                ps = this.connection.prepareStatement("SELECT table_schema, table_name, data_length, index_length FROM information_schema.TABLES WHERE TABLE_NAME = '" + table + "' AND TABLE_SCHEMA = '" + this.database_table + "' LIMIT 1;");
                rs = ps.executeQuery();
                if (rs.next()) {
                    size += rs.getLong("data_length");
                    size += rs.getLong("index_length");
                }
            }
            else {
                size += this.database.length();
            }
        }
        catch (SQLException ex) {
            throw new CoreException(ex, "Failed to get the table '" + table + "' size");
        }
        finally {
            closeQuietly(ps);
            closeQuietly(rs);
        }
        return size;
    }
    
    public long getTableFree(final String table) throws CoreException {
        long size = 0L;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final DBType type = this.type;
            final DBType type2 = this.type;
            if (type.equals(DBType.MySQL)) {
                ps = this.connection.prepareStatement("SELECT table_schema, table_name, data_free FROM information_schema.TABLES WHERE TABLE_NAME = '" + table + "' AND TABLE_SCHEMA = '" + this.database_table + "' LIMIT 1;");
                rs = ps.executeQuery();
                if (rs.next()) {
                    size += rs.getLong("data_free");
                }
            }
            else {
                size += this.database.getFreeSpace();
            }
        }
        catch (SQLException ex) {
            throw new CoreException(ex, "Failed to get the table '" + table + "' free space");
        }
        finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
        return size;
    }
    
    public void connect() throws CoreException {
        final Communicator com = this.plugin.getCommunicator();
        if (this.engine.equalsIgnoreCase("MySQL")) {
            final DBType type = this.type;
            this.type = DBType.MySQL;
        }
        else if (this.engine.equalsIgnoreCase("H2")) {
            final DBType type2 = this.type;
            this.type = DBType.H2;
        }
        else {
            final DBType type3 = this.type;
            this.type = DBType.SQLite;
        }
        com.log("[TAG] Connecting to the " + this.type + " database...", new Object[0]);
        final DBType type4 = this.type;
        final DBType type5 = this.type;
        if (type4 == DBType.MySQL) {
            this.connection = this.getMySQLConnection();
        }
        else {
            final DBType type6 = this.type;
            final DBType type7 = this.type;
            if (type6 == DBType.SQLite) {
                this.connection = this.getSQLiteConnection();
            }
            else {
                this.connection = this.getH2Connection();
            }
        }
        if (this.connection != null) {
            try {
                this.connection.setAutoCommit(false);
                this.commit();
            }
            catch (SQLException ex) {
                throw new CoreException(ex, "Failed to commit the " + this.type + " database");
            }
            this.kill.set(false);
            this.queue();
            this.garbage();
            this.keepAliveTask();
            com.log("[TAG] " + this.type + " database connected Successfuly!", new Object[0]);
            this.createTable("CREATE TABLE IF NOT EXISTS `" + this.prefix + "internal` (version INT);");
        }
    }
    
    public Connection getSQLiteConnection() throws CoreException {
        return this.getSQLiteConnection(new File(this.plugin.getDataFolder(), "database.db"));
    }
    
    public Connection getSQLiteConnection(final File sqlite) throws CoreException {
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException ex) {
            throw new CoreException(ex, "You don't have the required " + this.type + " driver");
        }
        try {
            sqlite.createNewFile();
        }
        catch (IOException ex2) {
            throw new CoreException(ex2, "Failed to create the " + this.type + " file");
        }
        this.database = sqlite;
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + sqlite.getAbsolutePath());
        }
        catch (SQLException ex3) {
            throw new CoreException(ex3, "Failed open the " + this.type + " connection");
        }
    }
    
    public Connection getMySQLConnection() throws CoreException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException ex) {
            throw new CoreException(ex, "You don't have the required " + this.type + " driver");
        }
        final String url = "jdbc:mysql://" + this.database_host + ":" + this.database_port + "/" + this.database_table + "?autoReconnect=true";
        try {
            return DriverManager.getConnection(url, this.database_user, this.database_pass);
        }
        catch (SQLException ex2) {
            throw new CoreException(ex2, "Failed open the " + this.type + " connection");
        }
    }
    
    public Connection getH2Connection() throws CoreException {
        return this.getH2Connection(this.plugin.getDataFolder(), this.plugin.getName());
    }
    
    public Connection getH2Connection(final File dir, final String name) throws CoreException {
        final Communicator com = this.plugin.getCommunicator();
        final File h2 = new File("lib", "h2.jar");
        if (!h2.exists()) {
            com.log("[TAG] You must have the h2.jar library file in your lib folder!", new Object[0]);
            com.log("[TAG] Download it here: \n http://hsql.sourceforge.net/m2-repo/com/h2database/h2/1.3.170/h2-1.3.170.jar", new Object[0]);
            return this.getSQLiteConnection();
        }
        try {
            Class.forName("org.h2.Driver");
        }
        catch (ClassNotFoundException ex) {
            throw new CoreException(ex, "You don't have the required " + this.type + " driver");
        }
        try {
            if (dir.isFile()) {
                dir.createNewFile();
            }
            dir.mkdirs();
        }
        catch (Exception ex2) {
            throw new CoreException(ex2, "Failed to create the " + this.type + " file");
        }
        this.database = dir;
        try {
            if (!dir.isFile()) {
                return DriverManager.getConnection("jdbc:h2:" + dir.getAbsolutePath() + File.separator + name + ";MODE=MySQL;IGNORECASE=TRUE", "sa", "");
            }
            return DriverManager.getConnection("jdbc:h2:file:" + dir.getAbsolutePath() + ";MODE=MySQL;IGNORECASE=TRUE", "sa", "");
        }
        catch (SQLException ex3) {
            throw new CoreException(ex3, "Failed open the " + this.type + " connection");
        }
    }
    
    public void disconnect(final boolean fix) throws CoreException {
        final Communicator com = this.plugin.getCommunicator();
        com.log("[TAG] Closing the " + this.type + " connection...", new Object[0]);
        if (!fix) {
            this.lock.set(true);
            if (!this.queue.isEmpty()) {
                com.log("[TAG] Queue isn't empty! Running the remaining queue...", new Object[0]);
                for (final World world : Bukkit.getWorlds()) {
                    world.save();
                }
                Bukkit.savePlayers();
                double process = 0.0;
                final double total = this.queue.size();
                double done = total - this.queue.size();
                double last = 0.0;
                this.commit();
                this.setAutoCommit(false);
                while (!this.queue.isEmpty()) {
                    done = total - this.queue.size();
                    final String query = this.queue.poll();
                    if (query == null) {
                        continue;
                    }
                    process = done / total * 100.0;
                    if (process - last > 1.0) {
                        System.gc();
                        com.log("[TAG] Processed {0} of {1} queries, {2}%", done, total, String.format("%d", (int)process));
                        last = process;
                    }
                    this.execute(query, new Object[0]);
                }
                this.commit();
                System.gc();
            }
        }
        this.kill.set(true);
        try {
            if (this.connection != null) {
                this.commit();
                this.connection.close();
                if (this.connection.isClosed()) {
                    com.log("[TAG] " + this.type + " connection closed successfuly!", new Object[0]);
                }
            }
        }
        catch (SQLException ex) {
            throw new CoreException(ex, "Can't close the " + this.type + " connection");
        }
    }
    
    public void fix() throws CoreException {
        final Communicator com = this.plugin.getCommunicator();
        if (this.fix > 3) {
            com.log("[TAG] Failed to fix the {0} connection after 3 attempts, shutting down...", new Object[0]);
            this.plugin.getPluginLoader().disablePlugin((Plugin)this.plugin);
            return;
        }
        ++this.fix;
        com.log("[TAG] The {0} database is down, reconnecting...", this.type);
        this.disconnect(true);
        this.connect();
        if (this.isOk()) {
            com.log("[TAG] {0} database is now up and running!", this.type);
            this.fix = 0;
        }
        else {
            com.log("[TAG] Failed to fix the {0} connection!, attempt {1} of 3.", this.type, this.fix);
        }
    }
    
    public boolean isOk() throws CoreException {
        if (this.connection == null) {
            return false;
        }
        try {
            if (this.connection.isClosed()) {
                return false;
            }
            if (!this.isAlive()) {
                return false;
            }
        }
        catch (SQLException ex) {
            throw new CoreException(ex, "Failed to check if the " + this.type + " connection is up");
        }
        return true;
    }
    
    public void createTable(final String query) throws CoreException {
        this.createTable(this.connection, query, this.type);
    }
    
    public void createTable(final Connection connection, String query, final DBType type) throws CoreException {
        Statement st = null;
        try {
            if (query.contains("{auto}")) {
                query = query.replace("{auto}", this.getAutoVariable(type));
            }
            st = connection.createStatement();
            st.executeUpdate(query);
            this.commit();
        }
        catch (SQLException ex) {
            throw new CoreException(ex, "Failed to create table in the " + type + " database, query: " + query);
        }
        finally {
            closeQuietly(st);
        }
    }
    
    public void createIndex(final String query) throws CoreException {
        this.createIndex(this.connection, query);
    }
    
    public void createIndex(final Connection connection, final String query) throws CoreException {
        Statement st = null;
        try {
            st = connection.createStatement();
            st.executeUpdate(query);
            this.commit();
        }
        catch (SQLException ex) {
            if (ex.getMessage().contains("syntax") || ex.getMessage().contains("SYNTAX")) {
                throw new CoreException(ex, "Failed to create index in the " + this.type + " database, query: " + query);
            }
        }
        finally {
            closeQuietly(st);
        }
    }
    
    public void incrementVersion(final int version) throws CoreException {
        this.execute("DELETE FROM `" + this.prefix + "internal`", new Object[0]);
        this.execute("INSERT INTO `" + this.prefix + "internal` VALUES ('" + version + "');", new Object[0]);
    }
    
    public boolean isUpdateAvailable() throws CoreException {
        return this.getLatestVersion() > this.getCurrentVersion();
    }
    
    public int getLatestVersion() {
        return this.version;
    }
    
    public int getCurrentVersion() throws CoreException {
        int ret = -1;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = this.connection.prepareStatement("SELECT version FROM `" + this.prefix + "internal`;");
            rs = ps.executeQuery();
            if (rs.next()) {
                ++this.reads;
                ret = rs.getInt("version");
            }
            else {
                this.execute("INSERT INTO `" + this.prefix + "internal` VALUES ('" + this.version + "');", new Object[0]);
                ret = this.version;
            }
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Can't retrieve " + this.type + " database version");
        }
        finally {
            closeQuietly(ps);
            closeQuietly(rs);
        }
        return ret;
    }
    
    public void queue(final String query) {
        this.queue.add(query);
    }
    
    public void execute(String query, final Object... objects) throws CoreException {
        if (objects != null && objects.length > 0) {
            query = MessageFormat.format(query, objects);
        }
        if (!this.allow_mainthread && Thread.currentThread() == Core.main_thread) {
            throw new IllegalStateException("This method cannot be cast from the main thread!");
        }
        PreparedStatement ps = null;
        try {
            ps = this.connection.prepareStatement(query);
            ps.execute();
        }
        catch (SQLException ex) {
            throw new CoreException(ex, "Can't write in the " + this.type + " database, query: " + query);
        }
        finally {
            this.closeLater(query, ps);
        }
    }
    
    public PreparedStatement getQuery(String query, final Object... objects) throws CoreException {
        if (objects != null && objects.length > 0) {
            query = MessageFormat.format(query, objects);
        }
        if (!this.allow_mainthread && Thread.currentThread() == Core.main_thread) {
            throw new IllegalStateException("This method cannot be cast from the main thread!");
        }
        try {
            PreparedStatement ps = this.prepare(query);
            try {
                ps.execute();
            }
            catch (Throwable ex) {
                if (!ex.getMessage().toLowerCase().contains("closed")) {
                    ex.printStackTrace();
                }
                else {
                    ps = this.connection.prepareStatement(query);
                    ps.execute();
                }
            }
            ++this.reads;
            return ps;
        }
        catch (Exception ex2) {
            this.verify(ex2);
            throw new CoreException(ex2, "Can't read the " + this.type + " database, query: " + query);
        }
    }
    
    public boolean hasTable(final String table) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = this.connection.prepareStatement("SELECT * FROM `" + table + "` LIMIT 1;");
            rs = ps.executeQuery();
            return rs.next();
        }
        catch (Exception ex) {
            return false;
        }
        finally {
            closeQuietly(ps);
            closeQuietly(rs);
        }
    }
    
    private int getQueueSpeed() {
        if (this.lock.get()) {
            return this.queue.size();
        }
        int count = (int)(this.queue.size() * this.queue_speed);
        if (count < 100) {
            count = 100;
        }
        if (count > 10000) {
            count = 10000;
        }
        return count;
    }
    
    private void queue() {
        for (int j = 1; j < this.queue_threads + 1; ++j) {
            final Thread t = new CoreSQLThread();
            t.setPriority(1);
            t.setName(this.plugin.getName() + " Database Task #" + j);
            t.start();
        }
    }
    
    public void commit() throws CoreException {
        try {
            if (!this.connection.getAutoCommit()) {
                this.connection.commit();
            }
        }
        catch (SQLException ex) {
            this.verify(ex);
            throw new CoreException(ex, "Can't commit the " + this.type + " database");
        }
    }
    
    public void setAutoCommit(final boolean auto) throws CoreException {
        try {
            if (this.connection != null) {
                this.connection.setAutoCommit(auto);
            }
        }
        catch (SQLException ex) {
            this.verify(ex);
            throw new CoreException(ex, "Can't set auto commit status the the " + this.type + " database");
        }
    }
    
    public void verify(final Exception ex) {
        try {
            if (!this.isOk()) {
                this.fix();
            }
        }
        catch (CoreException ex2) {
            this.plugin.getCommunicator().error(ex2);
        }
    }
    
    public PreparedStatement prepare(final String query) throws CoreException {
        PreparedStatement ps = null;
        if (this.cache.containsKey(query)) {
            return (PreparedStatement)this.cache.get(query);
        }
        try {
            ps = this.connection.prepareStatement(query);
        }
        catch (SQLException ex) {
            this.plugin.getCommunicator().error(ex, "Failed to prepare the query: " + query, new Object[0]);
        }
        if (ps != null) {
            this.cache.put(query, ps);
        }
        return ps;
    }
    
    private void keepAliveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this.plugin, (Runnable)new Runnable() {
            public void run() {
                if (!CoreSQLDatabase.this.isAlive()) {
                    try {
                        CoreSQLDatabase.this.fix();
                    }
                    catch (CoreException ex) {
                        CoreSQLDatabase.this.plugin.getCommunicator().error(ex);
                    }
                }
            }
        }, 6000L, 6000L);
    }
    
    private boolean isAlive() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = this.connection.prepareStatement("SELECT version FROM `" + this.prefix + "internal`;");
            rs = ps.executeQuery();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
        return true;
    }
    
    private void garbage() {
        Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this.plugin, (Runnable)new Runnable() {
            public void run() {
                final List<Statement> values = new ArrayList<Statement>(CoreSQLDatabase.this.cache.values());
                CoreSQLDatabase.this.cache.clear();
                final Iterator<Statement> it = values.iterator();
                while (it.hasNext()) {
                    try {
                        it.next().close();
                        it.remove();
                    }
                    catch (SQLException ex) {}
                }
                values.clear();
            }
        }, 3600L, 3600L);
    }
    
    public void closeLater(final String query, final Statement st) {
        if (st != null) {
            try {
                this.cache.put(query, st);
            }
            catch (Throwable t) {}
        }
    }
    
    public void closeLater(final Statement st) {
        if (st != null) {
            try {
                this.cache.put(Long.toString(System.nanoTime()), st);
            }
            catch (Throwable t) {}
        }
    }
    
    public static void closeQuietly(final Statement st) {
        if (st != null) {
            try {
                st.close();
            }
            catch (Throwable t) {}
        }
    }
    
    public static void closeQuietly(final ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            }
            catch (Throwable t) {}
        }
    }
    
    private class CoreSQLThread extends Thread
    {
        public void run() {
            boolean commited = false;
            int count = 0;
            while (!CoreSQLDatabase.this.kill.get()) {
                String query = null;
                try {
                    if (CoreSQLDatabase.this.queue.isEmpty()) {
                        if (!commited) {
                            CoreSQLDatabase.this.commit();
                            commited = true;
                        }
                        Thread.sleep(1000L);
                    }
                    else {
                        query = CoreSQLDatabase.this.queue.poll();
                        if (query == null) {
                            if (!commited) {
                                CoreSQLDatabase.this.commit();
                                commited = true;
                            }
                            Thread.sleep(1000L);
                        }
                        else {
                            ++count;
                            CoreSQLDatabase.this.execute(query, new Object[0]);
                            commited = false;
                            if (!CoreSQLDatabase.this.lock.get()) {
                                Thread.sleep(50L);
                            }
                            if (count < CoreSQLDatabase.this.getQueueSpeed()) {
                                continue;
                            }
                            if (!commited) {
                                CoreSQLDatabase.this.commit();
                                commited = true;
                            }
                            count = 0;
                            Thread.sleep(1000L);
                        }
                    }
                }
                catch (CoreException ex) {
                    CoreSQLDatabase.this.plugin.getCommunicator().error(ex);
                }
                catch (InterruptedException ex2) {}
            }
        }
    }
    
    public enum DBType
    {
        MySQL, 
        SQLite, 
        H2;
    }
}
