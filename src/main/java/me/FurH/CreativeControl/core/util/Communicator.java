package me.FurH.CreativeControl.core.util;

import me.FurH.CreativeControl.core.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.core.exceptions.*;
import org.bukkit.command.*;
import java.text.*;
import me.FurH.CreativeControl.core.time.*;
import java.io.*;
import me.FurH.CreativeControl.core.database.*;
import org.bukkit.*;
import org.bukkit.plugin.*;
import java.util.*;

public class Communicator
{
    private boolean communicator_quiet;
    private boolean communicator_debug;
    private String tag;
    private CorePlugin plugin;
    
    public void setQuiet(final boolean communicator_quiet) {
        this.communicator_quiet = communicator_quiet;
    }
    
    public void setDebug(final boolean communicator_debug) {
        this.communicator_debug = communicator_debug;
    }
    
    public void setTag(final String tag) {
        this.tag = tag;
    }
    
    public Communicator(final CorePlugin plugin, final String tag) {
        super();
        this.communicator_quiet = false;
        this.communicator_debug = false;
        this.tag = "&8[&aFCoreLib&8]&7:";
        this.plugin = plugin;
        this.tag = tag;
    }
    
    public void broadcast(final String message, final boolean console, final Object... objects) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.msg((CommandSender)player, message, objects);
        }
        if (console) {
            this.log(message, objects);
        }
    }
    
    public void broadcast(final String message, final String permission, final boolean console, final Object... objects) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                this.msg((CommandSender)player, message, objects);
            }
        }
        if (console) {
            this.log(message, objects);
        }
    }
    
    public void msg(final CommandSender sender, final String message, final Object... objects) {
        if (message == null || "".equals(message)) {
            return;
        }
        if (sender != null && !this.communicator_quiet) {
            sender.sendMessage(this.format(message, objects));
        }
        else {
            this.log(message, objects);
        }
    }
    
    public void error(final Exception ex) {
        String message = ex.getMessage();
        if (message == null) {
            message = "error";
        }
        this.error(ex, message, new Object[0]);
    }
    
    public void error(Exception ex, String message, final Object... objects) {
        if (!(ex instanceof CoreException)) {
            ex = new CoreException(ex, message);
        }
        message = this.format(message, objects);
        this.log(message, LogType.SEVERE, objects);
        this.log("[TAG] This error is avaliable at: plugins/{0}/error/error-{1}.txt", LogType.SEVERE, this.plugin.getDescription().getName(), this.stack((CoreException)ex));
    }
    
    public void severe(final String message, final Object... objects) {
        this.log(message, LogType.SEVERE, objects);
    }
    
    public void warning(final String message, final Object... objects) {
        this.log(message, LogType.WARNING, objects);
    }
    
    public void debug(final String message, final Object... objects) {
        this.log(message, LogType.DEBUG, objects);
    }
    
    public void log(final String message, final Object... objects) {
        this.log(message, LogType.INFO, objects);
    }
    
    public void log(final String message, final LogType type, final Object... objects) {
        if (message == null || "".equals(message)) {
            return;
        }
        final ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        if (type == LogType.INFO) {
            console.sendMessage(this.format(message, objects));
        }
        else if (type == LogType.SEVERE) {
            console.sendMessage(this.format("&4" + message, objects));
        }
        else if (type == LogType.WARNING) {
            console.sendMessage(this.format("&5" + message, objects));
        }
        else if (type == LogType.DEBUG && this.communicator_debug) {
            console.sendMessage(this.format("&3" + message, objects));
        }
    }
    
    public String colors(final String message) {
        return message.replaceAll("&([0-9a-fk-or])", "§$1");
    }
    
    public String format(String message, final Object... objects) {
        if (objects != null && objects.length > 0) {
            message = MessageFormat.format(message, objects);
        }
        if (message.contains("[TAG]")) {
            message = message.replaceAll("\\[TAG\\]", this.tag);
        }
        return this.colors(message);
    }
    
    private String stack(final CoreException ex) {
        final String format1 = TimeUtils.getSimpleFormatedTimeWithMillis(System.currentTimeMillis());
        File data = new File(this.plugin.getDataFolder() + File.separator + "error");
        if (!data.exists()) {
            data.mkdirs();
        }
        data = new File(data.getAbsolutePath(), "error-" + format1 + ".txt");
        if (!data.exists()) {
            try {
                data.createNewFile();
            }
            catch (IOException e) {
                this.log("Failed to create new log file, {0} .", e.getMessage());
            }
        }
        try {
            final String l = System.getProperty("line.separator");
            final String format2 = TimeUtils.getFormatedTime(System.currentTimeMillis());
            final FileWriter fw = new FileWriter(data, true);
            final BufferedWriter bw = new BufferedWriter(fw);
            final Runtime runtime = Runtime.getRuntime();
            final File root = new File("/");
            int creative = 0;
            int survival = 0;
            final int totalp = Bukkit.getOnlinePlayers().size();
            for (final Player p : Bukkit.getOnlinePlayers()) {
                if (p.getGameMode().equals((Object)GameMode.CREATIVE)) {
                    ++creative;
                }
                else {
                    ++survival;
                }
            }
            final CoreSQLDatabase db = this.plugin.coredatabase;
            final StackTraceElement[] thread1 = CorePlugin.main_thread.getStackTrace();
            final StackTraceElement[] core = ex.getCoreStackTrace();
            final StackTraceElement[] error = ex.getStackTrace();
            final StackTraceElement[] thread2 = ex.getThreadStackTrace();
            bw.write(format2 + l);
            bw.write("\t=============================[ ERROR INFORMATION ]=============================" + l);
            bw.write("\t- Plugin: " + this.plugin.getDescription().getFullName() + l);
            bw.write("\t- Uptime: " + Utils.getServerUptime() + l);
            bw.write("\t- Players: " + totalp + " (" + creative + " Creative, " + survival + " Survival)" + l);
            bw.write("\t=============================[ HARDWARE SETTINGS ]=============================" + l);
            bw.write("\t\tJava: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " " + System.getProperty("java.vendor.url") + l);
            bw.write("\t\tSystem: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + l);
            bw.write("\t\tProcessors: " + runtime.availableProcessors() + l);
            bw.write("\t\tMemory: " + l);
            bw.write("\t\t\tFree: " + Utils.getFormatedBytes(runtime.freeMemory()) + l);
            bw.write("\t\t\tTotal: " + Utils.getFormatedBytes(runtime.totalMemory()) + l);
            bw.write("\t\t\tMax: " + Utils.getFormatedBytes(runtime.maxMemory()) + l);
            bw.write("\t\tStorage: " + l);
            bw.write("\t\t\tTotal: " + Utils.getFormatedBytes(root.getTotalSpace()) + l);
            bw.write("\t\t\tFree: " + Utils.getFormatedBytes(root.getFreeSpace()) + l);
            if (db != null) {
                bw.write("\t=============================[ SQL INFORMATIONS ]=============================" + l);
                bw.write("\t\tServer Type: " + db.type.toString() + l);
                bw.write("\t\tLocalHost: " + ("localhost".equals(db.database_host) || "127.0.0.1".equals(db.database_host) || db.database_host.equals(Bukkit.getIp())) + l);
                bw.write("\t\tQueue speed: " + db.queue_speed + l);
                bw.write("\t\tQueue threads: " + db.queue_threads + l);
                bw.write("\t\tDatabase ping: " + ((db.type == CoreSQLDatabase.DBType.MySQL) ? db.ping() : "<0") + l);
                bw.write("\t\tDatabase version: " + db.version + l);
                bw.write("              Is Update Available: " + db.isUpdateAvailable() + l);
            }
            bw.write("\t=============================[ INSTALLED PLUGINS ]=============================" + l);
            bw.write("\tPlugins:" + l);
            for (final Plugin x : Bukkit.getServer().getPluginManager().getPlugins()) {
                bw.write("\t\t- " + x.getDescription().getFullName() + l);
            }
            bw.write("\t=============================[  LOADED   WORLDS  ]=============================" + l);
            bw.write("\tWorlds:" + l);
            for (final World w : Bukkit.getServer().getWorlds()) {
                bw.write("\t\t" + w.getName() + ":" + l);
                bw.write("\t\t\tEnvioronment: " + w.getEnvironment().toString() + l);
                bw.write("\t\t\tPlayer Count: " + w.getPlayers().size() + l);
                bw.write("\t\t\tEntity Count: " + w.getEntities().size() + l);
                bw.write("\t\t\tLoaded Chunks: " + w.getLoadedChunks().length + l);
            }
            bw.write("\t=============================[ MAIN  STACKTRACE ]=============================" + l);
            for (final StackTraceElement element : thread1) {
                bw.write("\t\t- " + element.toString() + l);
            }
            bw.write("\t=============================[ CORE  STACKTRACE ]=============================" + l);
            bw.write("\t- " + ex.getCoreMessage() + " [ " + ex.getCoreMessage().getClass().getSimpleName() + " ]" + l);
            for (final StackTraceElement element : core) {
                bw.write("\t\t- " + element.toString() + l);
            }
            bw.write("\t=============================[ ERROR STACKTRACE ]=============================" + l);
            bw.write("\t- " + ex.getMessage() + " [ " + ex.getCause().getClass().getSimpleName() + " ]" + l);
            for (final StackTraceElement element : error) {
                bw.write("\t\t- " + element.toString() + l);
            }
            bw.write("\t=============================[ EXTRA STACKTRACE ]=============================" + l);
            for (final StackTraceElement element : thread2) {
                bw.write("\t\t- " + element.toString() + l);
            }
            bw.write("\t=============================[ END OF STACKTRACE ]=============================" + l);
            bw.write(format2);
            bw.close();
            fw.close();
        }
        catch (IOException e) {
            this.log("Failed to write in the log file, {0}", e.getMessage());
        }
        catch (CoreException ex2) {
            ex2.printStackTrace();
        }
        return format1;
    }
    
    public enum LogType
    {
        INFO, 
        WARNING, 
        SEVERE, 
        DEBUG;
    }
}
