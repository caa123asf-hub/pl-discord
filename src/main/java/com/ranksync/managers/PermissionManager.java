package com.ranksync.managers;

import com.ranksync.RankSyncPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class PermissionManager {
    
    private final RankSyncPlugin plugin;
    private LuckPerms luckPerms;
    private Permission vaultPermission;
    private boolean useLuckPerms;
    
    public PermissionManager(RankSyncPlugin plugin) {
        this.plugin = plugin;
        setupPermissions();
    }
    
    private void setupPermissions() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            RegisteredServiceProvider<LuckPerms> provider = 
                    Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            
            if (provider != null) {
                luckPerms = provider.getProvider();
                useLuckPerms = true;
                plugin.getLogger().info("Hooked into LuckPerms!");
                return;
            }
        }
        
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> provider = 
                    Bukkit.getServicesManager().getRegistration(Permission.class);
            
            if (provider != null) {
                vaultPermission = provider.getProvider();
                useLuckPerms = false;
                plugin.getLogger().info("Hooked into Vault!");
                return;
            }
        }
        
        plugin.getLogger().warning("No permission plugin found! Plugin may not work correctly.");
    }
    
    public String getPrimaryGroup(UUID playerUuid) {
        if (useLuckPerms && luckPerms != null) {
            User user = luckPerms.getUserManager().getUser(playerUuid);
            if (user != null) {
                return user.getPrimaryGroup();
            }
        } else if (vaultPermission != null) {
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null) {
                return vaultPermission.getPrimaryGroup(player);
            }
        }
        
        return "default";
    }
    
    public void setPrimaryGroup(UUID playerUuid, String group) {
        if (useLuckPerms && luckPerms != null) {
            User user = luckPerms.getUserManager().getUser(playerUuid);
            if (user != null) {
                user.data().clear(node -> node instanceof InheritanceNode);
                
                InheritanceNode node = InheritanceNode.builder(group).build();
                user.data().add(node);
                
                luckPerms.getUserManager().saveUser(user);
            }
        } else if (vaultPermission != null) {
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null) {
                for (String existingGroup : vaultPermission.getPlayerGroups(player)) {
                    vaultPermission.playerRemoveGroup(player, existingGroup);
                }
                
                vaultPermission.playerAddGroup(player, group);
            }
        }
    }
    
    public boolean isAvailable() {
        return useLuckPerms || vaultPermission != null;
    }
}