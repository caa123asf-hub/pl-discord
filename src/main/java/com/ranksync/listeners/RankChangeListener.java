package com.ranksync.listeners;

import com.ranksync.RankSyncPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class RankChangeListener implements Listener {
    
    private final RankSyncPlugin plugin;
    
    public RankChangeListener(RankSyncPlugin plugin) {
        this.plugin = plugin;
        setupLuckPermsListener();
    }
    
    private void setupLuckPermsListener() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            return;
        }
        
        RegisteredServiceProvider<LuckPerms> provider = 
                Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        
        if (provider == null) {
            return;
        }
        
        LuckPerms luckPerms = provider.getProvider();
        EventBus eventBus = luckPerms.getEventBus();
        
        eventBus.subscribe(plugin, NodeAddEvent.class, this::onNodeAdd);
        eventBus.subscribe(plugin, NodeRemoveEvent.class, this::onNodeRemove);
    }
    
    private void onNodeAdd(NodeAddEvent event) {
        if (!(event.getTarget() instanceof User)) {
            return;
        }
        
        Node node = event.getNode();
        if (!(node instanceof InheritanceNode)) {
            return;
        }
        
        User user = (User) event.getTarget();
        UUID playerUuid = user.getUniqueId();
        
        if (!plugin.getLinkManager().isLinked(playerUuid)) {
            return;
        }
        
        if (!plugin.getConfigManager().isMinecraftToDiscord()) {
            return;
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getSyncManager().syncMinecraftToDiscord(playerUuid);
        }, 20L);
    }
    
    private void onNodeRemove(NodeRemoveEvent event) {
        if (!(event.getTarget() instanceof User)) {
            return;
        }
        
        Node node = event.getNode();
        if (!(node instanceof InheritanceNode)) {
            return;
        }
        
        User user = (User) event.getTarget();
        UUID playerUuid = user.getUniqueId();
        
        if (!plugin.getLinkManager().isLinked(playerUuid)) {
            return;
        }
        
        if (!plugin.getConfigManager().isMinecraftToDiscord()) {
            return;
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getSyncManager().syncMinecraftToDiscord(playerUuid);
        }, 20L);
    }
}