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
                                        MurderMysteryManager.startGame();
                                    }, 20L);
                                }, 20L);
                            }, 20L);
                        }, 20L);
                    }, 20L); // 20 ticks = 1 sec
                }
            }
        }
        return true;
    }
}
