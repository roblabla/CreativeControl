package me.FurH.CreativeControl.core.updater;

import me.FurH.CreativeControl.core.*;
import me.FurH.CreativeControl.core.number.*;
import me.FurH.CreativeControl.core.exceptions.*;
import javax.xml.parsers.*;
import java.net.*;
import org.w3c.dom.*;
import org.bukkit.*;
import org.bukkit.plugin.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import me.FurH.CreativeControl.core.util.*;

public class CoreUpdater
{
    private String url;
    private CorePlugin plugin;
    private String currentVersion;
    private String lastestVersion;
    private boolean updateAvailable;
    
    public CoreUpdater(final CorePlugin plugin, final String url) {
        super();
        this.updateAvailable = false;
        this.url = url;
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
    }
    
    public boolean isUpdateAvailable() {
        return this.updateAvailable;
    }
    
    public double getVersionNumber() throws CoreException {
        return NumberUtils.toDouble(this.getLastestVersion());
    }
    
    public String getLastestVersion() {
        String version = this.currentVersion;
        try {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new URL(this.url + "files.rss").openConnection().getInputStream());
            doc.getDocumentElement().normalize();
            final Node node = doc.getElementsByTagName("item").item(0);
            if (node != null && node.getNodeType() == 1) {
                this.lastestVersion = ((Element)node).getElementsByTagName("title").item(0).getChildNodes().item(0).getNodeValue();
                version = this.lastestVersion;
            }
        }
        catch (Exception ex) {
            return version;
        }
        return version;
    }
    
    public void setup() {
        Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this.plugin, (Runnable)new Runnable() {
            public void run() {
                CoreUpdater.this.checkUpdate();
            }
        }, 20L, 864000L);
    }
    
    private void checkUpdate() {
        try {
            final double newVersion = this.getVersionNumber();
            final double curVersion = NumberUtils.toDouble(this.currentVersion);
            if (curVersion < newVersion) {
                this.announce(null);
                this.updateAvailable = true;
            }
        }
        catch (CoreException ex) {}
    }
    
    public void announce(final Player player) {
        final Communicator com = this.plugin.getCommunicator();
        com.msg((CommandSender)player, "[TAG] New version found&8: &3{0}&f &8(&fYou have&8: &3{1}&8)", this.lastestVersion, this.currentVersion);
        com.msg((CommandSender)player, "&3Visit:&r " + this.url, new Object[0]);
    }
}
