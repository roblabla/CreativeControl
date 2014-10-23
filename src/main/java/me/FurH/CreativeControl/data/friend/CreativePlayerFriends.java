package me.FurH.CreativeControl.data.friend;

import me.FurH.CreativeControl.core.cache.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.database.*;
import java.util.*;
import me.FurH.CreativeControl.util.*;
import java.sql.*;

public class CreativePlayerFriends
{
    private CoreSafeCache<String, HashSet<String>> hascache;
    
    public CreativePlayerFriends() {
        super();
        this.hascache = new CoreSafeCache<String, HashSet<String>>();
    }
    
    public void uncache(final Player p) {
        this.hascache.remove(p.getName().toLowerCase());
    }
    
    public void clear() {
        this.hascache.clear();
    }
    
    public void saveFriends(final String player, final HashSet<String> friends) {
        final CreativeSQLDatabase db = CreativeControl.getDb();
        this.hascache.put(player, friends);
        final List<String> newFriends = new ArrayList<String>(friends);
        final String query = "UPDATE `" + db.prefix + "friends` SET friends = '" + newFriends + "' WHERE player = '" + db.getPlayerId(player.toLowerCase()) + "'";
        try {
            db.execute(query, new Object[0]);
        }
        catch (CoreException ex) {
            CreativeControl.plugin.getCommunicator().error(ex, "Failed to save " + player + "'s friends to the database", new Object[0]);
        }
        newFriends.clear();
    }
    
    public HashSet<String> getFriends(final String player) {
        HashSet<String> friends = this.hascache.get(player);
        final CreativeSQLDatabase db = CreativeControl.getDb();
        if (friends == null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db.getQuery("SELECT * FROM `" + db.prefix + "friends` WHERE player = '" + db.getPlayerId(player.toLowerCase()) + "'", new Object[0]);
                rs = ps.getResultSet();
                if (rs.next()) {
                    friends = CreativeUtil.toStringHashSet(rs.getString("friends"), ", ");
                }
                else {
                    friends = new HashSet<String>();
                    db.execute("INSERT INTO `" + db.prefix + "friends` (player, friends) VALUES ('" + db.getPlayerId(player.toLowerCase()) + "', '[]');", new Object[0]);
                }
                this.hascache.put(player, friends);
            }
            catch (SQLException ex) {
                CreativeControl.plugin.getCommunicator().error(ex, "Failed to get the data from the database", new Object[0]);
            }
            catch (CoreException ex2) {
                CreativeControl.plugin.getCommunicator().error(ex2, "Failed to get the data from the database", new Object[0]);
            }
            finally {
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (SQLException ex3) {}
                }
            }
        }
        return friends;
    }
}
