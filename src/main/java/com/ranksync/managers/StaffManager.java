package com.ranksync.managers;

import com.ranksync.RankSyncPlugin;
import com.ranksync.data.StaffData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffManager {
    
    private final RankSyncPlugin plugin;
    private final File staffFile;
    private FileConfiguration staffConfig;
    
    private final Map<String, StaffData> staffMap;
    
    public StaffManager(RankSyncPlugin plugin) {
        this.plugin = plugin;
        this.staffFile = new File(plugin.getDataFolder(), "staff.yml");
        this.staffMap = new HashMap<>();
        
        loadStaff();
    }
    
    public void loadStaff() {
        if (!staffFile.exists()) {
            plugin.saveResource("staff.yml", false);
        }
        
        staffConfig = YamlConfiguration.loadConfiguration(staffFile);
        staffMap.clear();
        
        ConfigurationSection staffSection = staffConfig.getConfigurationSection("staff");
        if (staffSection != null) {
            for (String key : staffSection.getKeys(false)) {
                String name = staffConfig.getString("staff." + key + ".name");
                String discordId = staffConfig.getString("staff." + key + ".discord-id");
                String minecraftGroup = staffConfig.getString("staff." + key + ".minecraft-group");
                String description = staffConfig.getString("staff." + key + ".description", "");
                
                if (name != null && discordId != null && minecraftGroup != null) {
                    StaffData staff = new StaffData(key, name, discordId, minecraftGroup, description);
                    staffMap.put(key, staff);
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + staffMap.size() + " staff members");
    }
    
    public void saveStaff() {
        for (StaffData staff : staffMap.values()) {
            String path = "staff." + staff.getKey();
            staffConfig.set(path + ".name", staff.getName());
            staffConfig.set(path + ".discord-id", staff.getDiscordId());
            staffConfig.set(path + ".minecraft-group", staff.getMinecraftGroup());
            staffConfig.set(path + ".description", staff.getDescription());
        }
        
        try {
            staffConfig.save(staffFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save staff.yml: " + e.getMessage());
        }
    }
    
    public void addStaff(String key, String name, String discordId, String minecraftGroup, String description) {
        StaffData staff = new StaffData(key, name, discordId, minecraftGroup, description);
        staffMap.put(key, staff);
        saveStaff();
    }
    
    public void removeStaff(String key) {
        staffMap.remove(key);
        staffConfig.set("staff." + key, null);
        saveStaff();
    }
    
    public StaffData getStaff(String key) {
        return staffMap.get(key);
    }
    
    public StaffData getStaffByDiscordId(String discordId) {
        for (StaffData staff : staffMap.values()) {
            if (staff.getDiscordId().equals(discordId)) {
                return staff;
            }
        }
        return null;
    }
    
    public StaffData getStaffByName(String name) {
        for (StaffData staff : staffMap.values()) {
            if (staff.getName().equalsIgnoreCase(name)) {
                return staff;
            }
        }
        return null;
    }
    
    public List<StaffData> getAllStaff() {
        return new ArrayList<>(staffMap.values());
    }
    
    public List<StaffData> getStaffByGroup(String minecraftGroup) {
        List<StaffData> result = new ArrayList<>();
        for (StaffData staff : staffMap.values()) {
            if (staff.getMinecraftGroup().equalsIgnoreCase(minecraftGroup)) {
                result.add(staff);
            }
        }
        return result;
    }
    
    public boolean isStaff(String discordId) {
        return getStaffByDiscordId(discordId) != null;
    }
}