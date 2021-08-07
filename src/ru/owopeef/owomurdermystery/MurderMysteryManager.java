package ru.owopeef.owomurdermystery;

import net.minecraft.server.v1_8_R3.Item;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class MurderMysteryManager
{
    static Plugin plugin = JavaPlugin.getPlugin(Main.class);
    public static String configKey = Main.configKey;
    public static String murder, detective, innocents, ghosts = "";
    public static void startGame()
    {
        murder = ""; detective = ""; innocents = ""; ghosts = "";
        List<Player> playerList = plugin.getServer().getWorld(plugin.getServer().getWorlds().get(0).getName()).getPlayers();
        int playerSize = playerList.size();
        int murderRandom = (int) (Math.random() * playerSize); int detectiveRandom = (int) (Math.random() * playerSize); int a = 0;
        while (murderRandom == detectiveRandom)
        {
            murderRandom = (int) (Math.random() * playerSize);
            detectiveRandom = (int) (Math.random() * playerSize);
        }
        while (a != playerSize)
        {
            if (a == murderRandom)
            {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clear " + playerList.get(a).getPlayer().getName());
                Material murderWeapon = Material.IRON_SWORD;
                try
                {
                    murderWeapon = Material.getMaterial(readConfig("maps", "0.murder_weapon").toUpperCase());
                }
                catch (Exception e)
                {
                    plugin.getLogger().warning("Material not found!\n" + e.getMessage());
                }
                int murderSlot = Integer.parseInt(readConfig(configKey, "murder_weapon_slot"));
                playerList.get(a).getInventory().setItem(murderSlot, new ItemStack(murderWeapon));
                murder = playerList.get(a).getName();
            }
            if (a == detectiveRandom)
            {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clear " + playerList.get(a).getPlayer().getName());
                int detectiveSlot = Integer.parseInt(readConfig(configKey, "detective_weapon_slot"));
                playerList.get(a).getInventory().setItem(detectiveSlot, new ItemStack(Item.getId(Item.getById(261))));
                detective = playerList.get(a).getName();
            }
            if (a != murderRandom && a != detectiveRandom)
            {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clear " + playerList.get(a).getPlayer().getName());
                innocents += playerList.get(a).getName() + ",";
            }
            int playerRoleInt = getRole(playerList.get(a).getName());
            String playerRole = "";
            if (playerRoleInt == 0)
            {
                playerRole = "ghost";
            }
            if (playerRoleInt == 1)
            {
                playerRole = "innocent";
            }
            if (playerRoleInt == 2)
            {
                playerRole = "detective";
            }
            if (playerRoleInt == 3)
            {
                playerRole = "murder";
            }
            String roleColor = readConfig(configKey, "roles." + playerRole + "_color");
            String roleResult = readConfig(configKey, "roles." + playerRole);
            String roleTitle = readConfig(configKey, "role_title").replace("{role_color}", roleColor).replace("{role}", roleResult);
            String roleSubtitle = readConfig(configKey, "roles." + playerRole + "_subtitle");
            String roleWarning = readConfig(configKey, "roles." + playerRole + "_warning");
            playerList.get(a).sendTitle(roleTitle, roleSubtitle);
            playerList.get(a).sendMessage(roleWarning);
            a++;
        }
    }
    public static void stopGame(Boolean innocentsWin)
    {
        murder = ""; detective = ""; innocents = ""; ghosts = "";
        if (innocentsWin)
        {
            String winInnocentMessage = readConfig(configKey, "innocent_win_message");
            plugin.getServer().broadcastMessage(winInnocentMessage);
        }
        else
        {
            String winMurderMessage = readConfig(configKey, "murder_win_message");
            plugin.getServer().broadcastMessage(winMurderMessage);
        }
    }
    public static int getRole(String nick)
    {
        plugin.getLogger().info("getRole("+nick+"), Detective: " + detective + ", Murder: " + murder);
        try
        {
            String[] players = innocents.split(",");
            int d = 0;
            while (d != players.length)
            {
                if (Objects.equals(nick, players[d]))
                {
                    return 1;
                }
                d++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (Objects.equals(nick, detective))
        {
            return 2;
        }
        if (Objects.equals(nick, murder))
        {
            return 3;
        }
        return 0;
    }
    public static void playerDeath(String nick)
    {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clear " + nick);
        innocents = innocents.replace(nick + ",", "");
        ghosts += nick + ",";
        Player player = Bukkit.getPlayer(nick);
        player.setAllowFlight(true);
        player.setFlying(true);
        playSound(player, Sound.SUCCESSFUL_HIT);
        String deathTitle = readConfig(configKey, "death_title");
        String deathSubTitle = readConfig(configKey, "death_subtitle");
        player.sendTitle(deathTitle, deathSubTitle);
        PotionEffect pe = PotionEffectType.INVISIBILITY.createEffect(99999999, 10);
        player.addPotionEffect(pe);
        int i = 0;
        String[] ghostsSplit = ghosts.split(",");
        while (i != ghostsSplit.length)
        {
            if (Objects.equals(ghostsSplit[i], detective) && innocents.split(",")[0].replace("null", "").length() == 0)
            {
                stopGame(false);
                break;
            }
            if (Objects.equals(ghostsSplit[i], murder))
            {
                stopGame(true);
                break;
            }
            if (Objects.equals(ghostsSplit[i], detective))
            {
                plugin.getServer().broadcastMessage("§cДетектив был убит!");
                break;
            }
            i++;
        }
    }
    public static void playSound(Player player, Sound sound)
    {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }
    public static String readConfig(String path, String parent)
    {
        return plugin.getConfig().get(path + "." + parent).toString();
    }
}
