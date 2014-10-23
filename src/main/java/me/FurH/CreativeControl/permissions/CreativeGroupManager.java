package me.FurH.CreativeControl.permissions;

//import org.anjocaido.groupmanager.*;
import org.bukkit.plugin.*;
import org.bukkit.entity.*;
//import org.anjocaido.groupmanager.permissions.*;

public class CreativeGroupManager implements CreativePermissionsInterface
{
    //private GroupManager groupManager;
    
    public CreativeGroupManager(final Plugin manager) {
        super();
      //  this.groupManager = (GroupManager)manager;
    }
    
    @Override
    public boolean hasPerm(final Player player, final String node) {
        //final AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldPermissions(player);
        //if (handler == null) {
            return player.hasPermission(node);
        //}
        //return handler.has(player, node);
    }
}
