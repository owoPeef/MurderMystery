package ru.owopeef.owomurdermystery.commands;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.owopeef.owomurdermystery.Main;
import ru.owopeef.owomurdermystery.MurderMysteryManager;
import ru.owopeef.owomurdermystery.utils.Config;

import java.util.Objects;

public class Commands implements CommandExecutor
{
    Plugin plugin = JavaPlugin.getPlugin(Main.class);
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) { return true; }
        Player player = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("murder_mystery"))
        {
            if (args.length == 0)
            {
                sender.sendMessage("Null");
                return true;
            }
            if (args.length == 1)
            {
                if (Objects.equals(args[0], "start"))
                {
                    plugin.getServer().broadcastMessage(ChatColor.RED + "Начало игры через 5 секунду");
                    MurderMysteryManager.playSound(player, Sound.CLICK);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        plugin.getServer().broadcastMessage(ChatColor.RED + "Начало игры через 4 секунды");
                        MurderMysteryManager.playSound(player, Sound.CLICK);
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                            plugin.getServer().broadcastMessage(ChatColor.RED + "Начало игры через 3 секунды");
                            MurderMysteryManager.playSound(player, Sound.CLICK);
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                plugin.getServer().broadcastMessage(ChatColor.RED + "Начало игры через 2 секунды");
                                MurderMysteryManager.playSound(player, Sound.CLICK);
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                    plugin.getServer().broadcastMessage(ChatColor.RED + "Начало игры через 1 секунду");
                                    MurderMysteryManager.playSound(player, Sound.CLICK);
                                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                        MurderMysteryManager.playSound(player, Sound.CLICK);
                                        try {
                                            MurderMysteryManager.startGame();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }, 20L);
                                }, 20L);
                            }, 20L);
                        }, 20L);
                    }, 20L); // 20 ticks = 1 sec
                }
            }
            if (args.length == 2)
            {
                if (Objects.equals(args[0], "add_gold"))
                {
                    try {
                        Config.appendConfig(player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ(), "maps", args[1], "gold");
                        player.sendMessage("§aВы успешно установили точку спавна золота! §e("+player.getLocation().getBlockX() + " " + player.getLocation().getBlockY() + " " + player.getLocation().getBlockZ()+")");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }
}
