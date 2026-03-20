package com.ranksync.managers;

import com.ranksync.RankSyncPlugin;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ConfigManager {
    
    private final RankSyncPlugin plugin;
    private FileConfiguration config;
    
    private String botToken;
    private String guildId;
    
    private boolean syncOnJoin;
    private int syncDelay;
    private boolean twoWaySync;
    private boolean minecraftToDiscord;
    private boolean discordToMinecraft;
    private boolean removeOldRoles;
    
    private int codeLength;
    private int codeExpireMinutes;
    
    private Map<String, RankMapping> rankMappings;
    
    public ConfigManager(RankSyncPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadConfig();
    }
    
    public void reload() {
        this.config = plugin.getConfig();
        loadConfig();
    }
    
    private void loadConfig() {
        botToken = config.getString("discord.bot-token", "");
        guildId = config.getString("discord.guild-id", "");
        
        syncOnJoin = config.getBoolean("sync-settings.sync-on-join", true);
        syncDelay = config.getInt("sync-settings.sync-delay-seconds", 5);
        twoWaySync = config.getBoolean("sync-settings.two-way-sync", true);
        minecraftToDiscord = config.getBoolean("sync-settings.minecraft-to-discord", true);
        discordToMinecraft = config.getBoolean("sync-settings.discord-to-minecraft", true);
        removeOldRoles = config.getBoolean("sync-settings.remove-old-roles", true);
        
        codeLength = config.getInt("verification.code-length", 6);
        codeExpireMinutes = config.getInt("verification.code-expire-minutes", 10);
        
        loadRankMappings();
    }
    
    private void loadRankMappings() {
        rankMappings = new HashMap<>();
        ConfigurationSection mappings = config.getConfigurationSection("rank-mappings");
        
        if (mappings != null) {
            for (String key : mappings.getKeys(false)) {
                String minecraftGroup = mappings.getString(key + ".minecraft-group");
                String discordRoleId = mappings.getString(key + ".discord-role-id");
                
                if (minecraftGroup != null && discordRoleId != null) {
                    rankMappings.put(key, new RankMapping(minecraftGroup, discordRoleId));
                }
            }
        }
    }
    
    public String getMessage(String path) {
        return plugin.getConfig().getString("messages." + path, "")
                .replace("&", "§");
    }
    
    public String getMessageWithPrefix(String path) {
        return getMessage("prefix") + getMessage(path);
    }
    
    public RankMapping getMappingByMinecraftGroup(String group) {
        for (RankMapping mapping : rankMappings.values()) {
            if (mapping.getMinecraftGroup().equalsIgnoreCase(group)) {
                return mapping;
            }
        }
        return null;
    }
    
    public RankMapping getMappingByDiscordRole(String roleId) {
        for (RankMapping mapping : rankMappings.values()) {
            if (mapping.getDiscordRoleId().equals(roleId)) {
                return mapping;
            }
        }
        return null;
    }
    
    @Getter
    public static class RankMapping {
        private final String minecraftGroup;
        private final String discordRoleId;
        
        public RankMapping(String minecraftGroup, String discordRoleId) {
            this.minecraftGroup = minecraftGroup;
            this.discordRoleId = discordRoleId;
        }
    }
}