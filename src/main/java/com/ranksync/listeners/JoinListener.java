package com.ranksync.listeners;

import com.ranksync.RankSyncPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    
    private final RankSyncPlugin plugin;
    
    public JoinListener(RankSyncPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getConfigManager().isSyncOnJoin()) {
            return;
        }

if (!plugin.getLinkManager().isLinked(player.getUniqueId())) {
            return;
        }
        
        if (!plugin.getDiscordManager().isConnected()) {
            return;
        }
        
        int delayTicks = plugin.getConfigManager().getSyncDelay() * 20;
        plugin.getSyncManager().syncPlayer(player.getUniqueId(), delayTicks);
    }
}