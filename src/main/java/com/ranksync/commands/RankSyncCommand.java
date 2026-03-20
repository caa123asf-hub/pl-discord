package com.ranksync.commands;

import com.ranksync.RankSyncPlugin;
import com.ranksync.data.LinkData;
import com.ranksync.data.StaffData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankSyncCommand implements CommandExecutor {
    
    private final RankSyncPlugin plugin;
    
    public RankSyncCommand(RankSyncPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§8§m                    §r §bRankSync §8§m                    ");
            sender.sendMessage("§f/ranksync link §8- §7Liên kết tài khoản Discord");
            sender.sendMessage("§f/ranksync unlink §8- §7Hủy liên kết tài khoản");
            sender.sendMessage("§f/ranksync sync §8- §7Đồng bộ tất cả (Admin)");
            sender.sendMessage("§f/ranksync staff <list/info/add/remove> §8- §7Quản lý staff (Admin)");
            sender.sendMessage("§f/ranksync reload §8- §7Tải lại cấu hình (Admin)");
            sender.sendMessage("§8§m                                              ");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "link":
                return handleLink(sender);
                
            case "unlink":
                return handleUnlink(sender);
                
            case "sync":
                return handleSync(sender);
                
            case "staff":
                return handleStaff(sender, args);
                
            case "reload":
                return handleReload(sender);
                
            default:
                sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("no-permission"));
                return true;
        }
    }
    
    private boolean handleLink(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể sử dụng lệnh này!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.getLinkManager().isLinked(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessageWithPrefix("link-already-linked"));
            return true;
        }
        
        String code = plugin.getLinkManager().generateVerificationCode(player.getUniqueId());
        
        String message = plugin.getConfigManager().getMessageWithPrefix("link-code-generated")
                .replace("{code}", code);
        player.sendMessage(message);
        
        String instruction = plugin.getConfigManager().getMessageWithPrefix("link-code-instruction")
                .replace("{code}", code);
        player.sendMessage(instruction);
        
        return true;
    }
    
    private boolean handleUnlink(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể sử dụng lệnh này!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getLinkManager().isLinked(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessageWithPrefix("unlink-not-linked"));
            return true;
        }
        
        plugin.getLinkManager().removeLink(player.getUniqueId());
        player.sendMessage(plugin.getConfigManager().getMessageWithPrefix("unlink-success"));
        
        return true;
    }
    
    private boolean handleSync(CommandSender sender) {
        if (!sender.hasPermission("ranksync.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("no-permission"));
            return true;
        }
        
        if (!plugin.getDiscordManager().isConnected()) {
            sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("discord-not-ready"));
            return true;
        }
        
        sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("sync-started"));
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int count = plugin.getSyncManager().syncAll();
            
            String message = plugin.getConfigManager().getMessageWithPrefix("sync-completed")
                    .replace("{count}", String.valueOf(count));
            sender.sendMessage(message);
        });
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("ranksync.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("no-permission"));
            return true;
        }
        
        plugin.reload();
        sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("reload-success"));
        
        return true;
    }
    
    private boolean handleStaff(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ranksync.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c/ranksync staff <list/info/add/remove>");
            return true;
        }
        
        switch (args[1].toLowerCase()) {
            case "list":
                return handleStaffList(sender);
                
            case "info":
                if (args.length < 3) {
                    sender.sendMessage("§c/ranksync staff info <key>");
                    return true;
                }
                return handleStaffInfo(sender, args[2]);
                
            case "add":
                if (args.length < 6) {
                    sender.sendMessage("§c/ranksync staff add <key> <name> <discord-id> <group> [description]");
                    return true;
                }
                String description = args.length > 6 ? String.join(" ", java.util.Arrays.copyOfRange(args, 6, args.length)) : "";
                return handleStaffAdd(sender, args[2], args[3], args[4], args[5], description);
                
            case "remove":
                if (args.length < 3) {
                    sender.sendMessage("§c/ranksync staff remove <key>");
                    return true;
                }
                return handleStaffRemove(sender, args[2]);
                
            default:
                sender.sendMessage("§c/ranksync staff <list/info/add/remove>");
                return true;
        }
    }
    
    private boolean handleStaffList(CommandSender sender) {
        sender.sendMessage("§8§m                    §r §bStaff List §8§m                    ");
        
        for (StaffData staff : plugin.getStaffManager().getAllStaff()) {
            sender.sendMessage("§f" + staff.getKey() + " §8- §7" + staff.getName() + " §8(§e" + staff.getMinecraftGroup() + "§8)");
            sender.sendMessage("  §8Discord ID: §7" + staff.getDiscordId());
            if (!staff.getDescription().isEmpty()) {
                sender.sendMessage("  §8Description: §7" + staff.getDescription());
            }
        }
        
        sender.sendMessage("§8§m                                              ");
        return true;
    }
    
    private boolean handleStaffInfo(CommandSender sender, String key) {
        StaffData staff = plugin.getStaffManager().getStaff(key);
        
        if (staff == null) {
            sender.sendMessage("§cStaff không tồn tại: " + key);
            return true;
        }
        
        sender.sendMessage("§8§m                    §r §bStaff Info §8§m                    ");
        sender.sendMessage("§fKey: §7" + staff.getKey());
        sender.sendMessage("§fName: §7" + staff.getName());
        sender.sendMessage("§fDiscord ID: §7" + staff.getDiscordId());
        sender.sendMessage("§fMinecraft Group: §7" + staff.getMinecraftGroup());
        sender.sendMessage("§fDescription: §7" + staff.getDescription());
        sender.sendMessage("§8§m                                              ");
        return true;
    }
    
    private boolean handleStaffAdd(CommandSender sender, String key, String name, String discordId, String group, String description) {
        if (plugin.getStaffManager().getStaff(key) != null) {
            sender.sendMessage("§cStaff đã tồn tại: " + key);
            return true;
        }
        
        plugin.getStaffManager().addStaff(key, name, discordId, group, description);
        sender.sendMessage("§aĐã thêm staff: §f" + name + " §8(§e" + key + "§8)");
        return true;
    }
    
    private boolean handleStaffRemove(CommandSender sender, String key) {
        if (plugin.getStaffManager().getStaff(key) == null) {
            sender.sendMessage("§cStaff không tồn tại: " + key);
            return true;
        }
        
        plugin.getStaffManager().removeStaff(key);
        sender.sendMessage("§aĐã xóa staff: " + key);
        return true;
    }
}