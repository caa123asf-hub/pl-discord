package com.ranksync.managers;

import com.ranksync.RankSyncPlugin;
import com.ranksync.data.LinkData;
import com.ranksync.data.VerificationCode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class LinkManager {
    
    private final RankSyncPlugin plugin;
    private final File linksFile;
    private FileConfiguration linksConfig;
    
    private final Map<UUID, LinkData> links;
    private final Map<String, VerificationCode> pendingVerifications;
    private final Random random;
    
    public LinkManager(RankSyncPlugin plugin) {
        this.plugin = plugin;
        this.linksFile = new File(plugin.getDataFolder(), "links.yml");
        this.links = new HashMap<>();
        this.pendingVerifications = new HashMap<>();
        this.random = new Random();
        
        loadLinks();
    }
    
    public void loadLinks() {
        if (!linksFile.exists()) {
            plugin.saveResource("links.yml", false);
        }
        
        linksConfig = YamlConfiguration.loadConfiguration(linksFile);
        links.clear();
        
        if (linksConfig.contains("links")) {
            for (String key : linksConfig.getConfigurationSection("links").getKeys(false)) {
                UUID playerUuid = UUID.fromString(key);
                String discordId = linksConfig.getString("links." + key + ".discord-id");
                long linkedAt = linksConfig.getLong("links." + key + ".linked-at");
                
                links.put(playerUuid, new LinkData(playerUuid, discordId, linkedAt));
            }
        }
        
        plugin.getLogger().info("Loaded " + links.size() + " linked accounts");
    }
    
    public void saveLinks() {
        for (LinkData link : links.values()) {
            String path = "links." + link.getPlayerUuid().toString();
            linksConfig.set(path + ".discord-id", link.getDiscordId());
            linksConfig.set(path + ".linked-at", link.getLinkedAt());
        }
        
        try {
            linksConfig.save(linksFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save links: " + e.getMessage());
        }
    }
    
    public String generateVerificationCode(UUID playerUuid) {
        String code = generateRandomCode();
        long expiresAt = System.currentTimeMillis() + 
                (plugin.getConfigManager().getCodeExpireMinutes() * 60 * 1000L);
        
        pendingVerifications.put(code, new VerificationCode(playerUuid, code, expiresAt));
        return code;
    }
    
    private String generateRandomCode() {
        int length = plugin.getConfigManager().getCodeLength();
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }
    
    public VerificationCode getVerificationCode(String code) {
        VerificationCode verificationCode = pendingVerifications.get(code);
        
        if (verificationCode != null && verificationCode.isExpired()) {
            pendingVerifications.remove(code);
            return null;
        }
        
        return verificationCode;
    }
    
    public void completeVerification(String code, String discordId) {
        VerificationCode verificationCode = pendingVerifications.get(code);
        
        if (verificationCode != null) {
            LinkData linkData = new LinkData(
                    verificationCode.getPlayerUuid(),
                    discordId,
                    System.currentTimeMillis()
            );
            
            links.put(verificationCode.getPlayerUuid(), linkData);
            pendingVerifications.remove(code);
            saveLinks();
        }
    }
    
    public void removeLink(UUID playerUuid) {
        links.remove(playerUuid);
        linksConfig.set("links." + playerUuid.toString(), null);
        saveLinks();
    }
    
    public LinkData getLink(UUID playerUuid) {
        return links.get(playerUuid);
    }
    
    public LinkData getLinkByDiscordId(String discordId) {
        for (LinkData link : links.values()) {
            if (link.getDiscordId().equals(discordId)) {
                return link;
            }
        }
        return null;
    }
    
    public boolean isLinked(UUID playerUuid) {
        return links.containsKey(playerUuid);
    }
    
    public Map<UUID, LinkData> getAllLinks() {
        return new HashMap<>(links);
    }
}