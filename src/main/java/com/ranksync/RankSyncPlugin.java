package com.ranksync;

import com.ranksync.commands.RankSyncCommand;
import com.ranksync.data.LinkData;
import com.ranksync.discord.DiscordManager;
import com.ranksync.listeners.JoinListener;
import com.ranksync.listeners.RankChangeListener;
import com.ranksync.managers.ConfigManager;
import com.ranksync.managers.LinkManager;
import com.ranksync.managers.PermissionManager;
import com.ranksync.managers.StaffManager;
import com.ranksync.managers.SyncManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class RankSyncPlugin extends JavaPlugin {
    
    private ConfigManager configManager;
    private LinkManager linkManager;
    private StaffManager staffManager;
    private DiscordManager discordManager;
    private PermissionManager permissionManager;
    private SyncManager syncManager;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.configManager = new ConfigManager(this);
        this.linkManager = new LinkManager(this);
        this.staffManager = new StaffManager(this);
        this.permissionManager = new PermissionManager(this);
        this.discordManager = new DiscordManager(this);
        this.syncManager = new SyncManager(this);
        
        if (!discordManager.connect()) {
            getLogger().severe("Failed to connect to Discord! Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        getCommand("ranksync").setExecutor(new RankSyncCommand(this));
        
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new RankChangeListener(this), this);
        
        getLogger().info("RankSyncPlugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (discordManager != null) {
            discordManager.disconnect();
        }
        
        if (linkManager != null) {
            linkManager.saveLinks();
        }
        
        getLogger().info("RankSyncPlugin has been disabled!");
    }
    
    public void reload() {
        reloadConfig();
        configManager.reload();
        linkManager.loadLinks();
        staffManager.loadStaff();
        
        if (discordManager.isConnected()) {
            discordManager.disconnect();
        }
        discordManager.connect();
    }
}