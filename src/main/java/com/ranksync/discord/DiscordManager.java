package com.ranksync.discord;

import com.ranksync.RankSyncPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class DiscordManager {
    
    private final RankSyncPlugin plugin;
    
    @Getter
    private JDA jda;
    
    @Getter
    private Guild guild;
    
    public DiscordManager(RankSyncPlugin plugin) {
        this.plugin = plugin;
    }
    
    public boolean connect() {
        String token = plugin.getConfigManager().getBotToken();
        
        if (token.isEmpty() || token.equals("YOUR_BOT_TOKEN_HERE")) {
            plugin.getLogger().severe("Bot token not configured in config.yml!");
            return false;
        }
        
        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .addEventListeners(new DiscordListener(plugin))
                    .build();
            
            jda.awaitReady();
            
            String guildId = plugin.getConfigManager().getGuildId();
            guild = jda.getGuildById(guildId);
            
            if (guild == null) {
                plugin.getLogger().severe("Guild not found with ID: " + guildId);
                return false;
            }
            
            plugin.getLogger().info("Connected to Discord! Guild: " + guild.getName());
            return true;
            
        } catch (InterruptedException e) {
            plugin.getLogger().severe("Failed to connect to Discord: " + e.getMessage());
            return false;
        }
    }
    
    public void disconnect() {
        if (jda != null) {
            jda.shutdown();
        }
    }
    
    public boolean isConnected() {
        return jda != null && guild != null;
    }
}