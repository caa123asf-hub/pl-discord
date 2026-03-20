package com.ranksync.managers;

import com.ranksync.RankSyncPlugin;
import com.ranksync.data.LinkData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SyncManager {
    
    private final RankSyncPlugin plugin;
    
    public SyncManager(RankSyncPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void syncMinecraftToDiscord(UUID playerUuid) {
        if (!plugin.getConfigManager().isMinecraftToDiscord()) {
            return;
        }
        
        LinkData link = plugin.getLinkManager().getLink(playerUuid);
        if (link == null) {
            return;
        }
        
        String minecraftGroup = plugin.getPermissionManager().getPrimaryGroup(playerUuid);
        ConfigManager.RankMapping mapping = plugin.getConfigManager()
                .getMappingByMinecraftGroup(minecraftGroup);
        
        if (mapping == null) {
            return;
        }
        
        Guild guild = plugin.getDiscordManager().getGuild();
        if (guild == null) {
            return;
        }
        
        guild.retrieveMemberById(link.getDiscordId()).queue(member -> {
            Role targetRole = guild.getRoleById(mapping.getDiscordRoleId());
            if (targetRole == null) {
                return;
            }
            
            List<Role> toAdd = new ArrayList<>();
            List<Role> toRemove = new ArrayList<>();
            
            if (!member.getRoles().contains(targetRole)) {
                toAdd.add(targetRole);
            }
            
            if (plugin.getConfigManager().isRemoveOldRoles()) {
                for (Role role : member.getRoles()) {
                    ConfigManager.RankMapping roleMapping = plugin.getConfigManager()
                            .getMappingByDiscordRole(role.getId());
                    
                    if (roleMapping != null && !role.equals(targetRole)) {
                        toRemove.add(role);
                    }
                }
            }
            
            if (!toAdd.isEmpty()) {
                guild.modifyMemberRoles(member, toAdd, toRemove).queue();
            } else if (!toRemove.isEmpty()) {
                guild.modifyMemberRoles(member, null, toRemove).queue();
            }
        }, throwable -> {
            plugin.getLogger().warning("Failed to sync Discord roles for " + playerUuid);
        });
    }
    
    public void syncDiscordToMinecraft(String discordId, List<String> roleIds) {
        if (!plugin.getConfigManager().isDiscordToMinecraft()) {
            return;
        }
        
        LinkData link = plugin.getLinkManager().getLinkByDiscordId(discordId);
        if (link == null) {
            return;
        }
        
        String highestRankGroup = null;
        
        for (String roleId : roleIds) {
            ConfigManager.RankMapping mapping = plugin.getConfigManager()
                    .getMappingByDiscordRole(roleId);
            
            if (mapping != null) {
                highestRankGroup = mapping.getMinecraftGroup();
                break;
            }
        }
        
        if (highestRankGroup == null) {
            return;
        }
        
        String currentGroup = plugin.getPermissionManager().getPrimaryGroup(link.getPlayerUuid());
        
        if (!currentGroup.equalsIgnoreCase(highestRankGroup)) {
            plugin.getPermissionManager().setPrimaryGroup(link.getPlayerUuid(), highestRankGroup);
            
            Player player = Bukkit.getPlayer(link.getPlayerUuid());
            if (player != null) {
                player.sendMessage(plugin.getConfigManager().getMessageWithPrefix("rank-synced"));
            }
        }
    }
    
    public int syncAll() {
        int synced = 0;
        
        for (LinkData link : plugin.getLinkManager().getAllLinks().values()) {
            syncMinecraftToDiscord(link.getPlayerUuid());
            synced++;
        }
        
        return synced;
    }
    
    public void syncPlayer(UUID playerUuid, int delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            syncMinecraftToDiscord(playerUuid);
        }, delayTicks);
    }
}