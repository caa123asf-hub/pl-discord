package com.ranksync.discord;

import com.ranksync.RankSyncPlugin;
import com.ranksync.data.LinkData;
import com.ranksync.data.VerificationCode;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class DiscordListener extends ListenerAdapter {
    
    private final RankSyncPlugin plugin;
    
    public DiscordListener(RankSyncPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        
        if (!event.isFromGuild()) {
            return;
        }
        
        String message = event.getMessage().getContentRaw();
        
        if (message.startsWith("/verify ") || message.startsWith("!verify ")) {
            String code = message.substring(8).trim().toUpperCase();
            
            VerificationCode verificationCode = plugin.getLinkManager().getVerificationCode(code);
            
            if (verificationCode == null) {
                event.getChannel().sendMessage(
                        event.getAuthor().getAsMention() + 
                        " ❌ Code không hợp lệ hoặc đã hết hạn!"
                ).queue();
                return;
            }
            
            String discordId = event.getAuthor().getId();
            LinkData existingLink = plugin.getLinkManager().getLinkByDiscordId(discordId);
            
            if (existingLink != null) {
                event.getChannel().sendMessage(
                        event.getAuthor().getAsMention() + 
                        " ❌ Tài khoản Discord của bạn đã được liên kết rồi!"
                ).queue();
                return;
            }
            
            plugin.getLinkManager().completeVerification(code, discordId);
            
            event.getChannel().sendMessage(
                    event.getAuthor().getAsMention() + 
                    " ✅ Đã liên kết tài khoản thành công!"
            ).queue();
            
            Player player = Bukkit.getPlayer(verificationCode.getPlayerUuid());
            if (player != null) {
                player.sendMessage(plugin.getConfigManager().getMessageWithPrefix("link-success"));
                
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getSyncManager().syncMinecraftToDiscord(player.getUniqueId());
                }, 20L);
            }
        }
    }
    
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (!plugin.getConfigManager().isTwoWaySync()) {
            return;
        }
        
        Member member = event.getMember();
        List<String> roleIds = member.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toList());
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getSyncManager().syncDiscordToMinecraft(member.getId(), roleIds);
        });
    }
    
    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        if (!plugin.getConfigManager().isTwoWaySync()) {
            return;
        }
        
        Member member = event.getMember();
        List<String> roleIds = member.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toList());
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getSyncManager().syncDiscordToMinecraft(member.getId(), roleIds);
        });
    }
}