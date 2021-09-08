package ru.owopeef.owomurdermystery.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.owopeef.owomurdermystery.MurderMysteryManager;
import ru.owopeef.owomurdermystery.utils.Config;

import java.util.Objects;

public class Commands implements CommandExecutor
{
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
                    if (player.hasPermission("murder_mystery.start"))
                    {
                        MurderMysteryManager.startGame();
                    }
                    else
                    {
                        player.sendMessage("§cУ вас недостаточно прав для использовании команды! (murder_mystery.start)");
                    }
                }
            }
            if (args.length == 2)
            {
                if (Objects.equals(args[0], "add_gold")) {
                    if (player.hasPermission("murder_mystery.add_gold")) {
                        try {
                            Config.appendConfig(player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ(), "maps", args[1], "gold");
                            player.sendMessage("§aВы успешно установили точку спавна золота! §e(" + player.getLocation().getBlockX() + " " + player.getLocation().getBlockY() + " " + player.getLocation().getBlockZ() + ")");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        player.sendMessage("§cУ вас недостаточно прав для использовании команды! (murder_mystery.add_gold)");
                    }
                }
            }
        }
        return true;
    }
}
