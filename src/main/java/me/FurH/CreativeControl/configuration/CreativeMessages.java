package me.FurH.CreativeControl.configuration;

import me.FurH.CreativeControl.core.configuration.*;
import me.FurH.CreativeControl.core.*;

public class CreativeMessages extends Configuration
{
    public String prefix_tag;
    public String mainode_restricted;
    public String blockplace_cantplace;
    public String blockmanager_belongs;
    public String blockmanager_unprotected;
    public String blockmanager_already;
    public String blockmanager_protected;
    public String blockmanager_removed;
    public String blockmanager_worldexcluded;
    public String blockmanager_excluded;
    public String blockmanager_limit;
    public String blockbreak_cantbreak;
    public String blockbreak_survival;
    public String blockbreak_creativeblock;
    public String limits_vehicles;
    public String region_welcome;
    public String region_farewell;
    public String region_unallowed;
    public String region_cant_change;
    public String blacklist_commands;
    public String selection_first;
    public String selection_second;
    
    public CreativeMessages(final CorePlugin plugin) {
        super(plugin);
        this.prefix_tag = "prefix.tag";
        this.mainode_restricted = "MainNode.restricted";
        this.blockplace_cantplace = "BlockPlace.cantplace";
        this.blockmanager_belongs = "BlockManager.belongsto";
        this.blockmanager_unprotected = "BlockManager.unprotected";
        this.blockmanager_already = "BlockManager.alreadyprotected";
        this.blockmanager_protected = "BlockManager.blockprotected";
        this.blockmanager_removed = "BlockManager.blockunprotected";
        this.blockmanager_worldexcluded = "BlockManager.worlddisabled";
        this.blockmanager_excluded = "BlockManager.typeexcluded";
        this.blockmanager_limit = "BlockManager.minutelimit";
        this.blockbreak_cantbreak = "BlockBreak.cantbreak";
        this.blockbreak_survival = "BlockBreak.nosurvival";
        this.blockbreak_creativeblock = "BlockBreak.creativeblock";
        this.limits_vehicles = "Limits.vehicles";
        this.region_welcome = "Regions.welcome";
        this.region_farewell = "Regions.farewell";
        this.region_unallowed = "Regions.survival";
        this.region_cant_change = "Regions.cant_here";
        this.blacklist_commands = "BlackList.commands";
        this.selection_first = "Selection.first_point";
        this.selection_second = "Selection.second_point";
    }
    
    public void load() {
        this.prefix_tag = this.getMessage("prefix.tag");
        this.mainode_restricted = this.getMessage("MainNode.restricted");
        this.blockplace_cantplace = this.getMessage("BlockPlace.cantplace");
        this.blockmanager_belongs = this.getMessage("BlockManager.belongsto");
        this.blockmanager_unprotected = this.getMessage("BlockManager.unprotected");
        this.blockmanager_already = this.getMessage("BlockManager.alreadyprotected");
        this.blockmanager_protected = this.getMessage("BlockManager.blockprotected");
        this.blockmanager_removed = this.getMessage("BlockManager.blockunprotected");
        this.blockmanager_worldexcluded = this.getMessage("BlockManager.worlddisabled");
        this.blockmanager_excluded = this.getMessage("BlockManager.typeexcluded");
        this.blockmanager_limit = this.getMessage("BlockManager.minutelimit");
        this.blockbreak_cantbreak = this.getMessage("BlockBreak.cantbreak");
        this.blockbreak_survival = this.getMessage("BlockBreak.nosurvival");
        this.blockbreak_creativeblock = this.getMessage("BlockBreak.creativeblock");
        this.limits_vehicles = this.getMessage("Limits.vehicles");
        this.region_welcome = this.getMessage("Regions.welcome");
        this.region_farewell = this.getMessage("Regions.farewell");
        this.region_unallowed = this.getMessage("Regions.unallowed");
        this.region_cant_change = this.getMessage("Regions.cant_here");
        this.blacklist_commands = this.getMessage("BlackList.commands");
        this.selection_first = this.getMessage("Selection.first_point");
        this.selection_second = this.getMessage("Selection.second_point");
        this.updateConfig();
    }
}
