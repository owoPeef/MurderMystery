package ru.owopeef.owomurdermystery;

import net.minecraft.server.v1_8_R3.Item;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.owopeef.owomurdermystery.utils.Config;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("deprecation")
public class MurderMysteryManager
{
    static Plugin plugin = JavaPlugin.getPlugin(Main.class);
    public static String configKey = Config.configKey;
    public static String murder, detective, innocents, ghosts = "";
    public static void startGame() throws IOException, InvalidConfigurationException {
        Timer timer = new Timer();
        Thread th = new Thread(() -> timer.schedule(new TimerTask() {
            public void run() {
                String[] split = Config.readConfigString("maps", "0", "gold").split(";");
                int randomInt = (int) (Math.random() * split.length - 1);
                String[] cords = split[randomInt].split(",");
                World w = Bukkit.getWorld("world");
                float x = Float.parseFloat(cords[0]);
                float y = Float.parseFloat(cords[1]);
                float z = Float.parseFloat(cords[2]);
                Location loc = new Location(w, x, y, z);
                ItemStack gold = new ItemStack(266);
                plugin.getServer().getWorld(w.getName()).dropItemNaturally(loc, gold);
                plugin.getServer().getPlayer("owoPeef1").sendMessage("§aGold spawned at " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
            }
        }, 0, 10000));
        th.start();
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
                    murderWeapon = Material.getMaterial(Config.readConfig(configKey, "murder_weapon").toUpperCase());
                }
                catch (Exception e)
                {
                    plugin.getLogger().warning("Material not found!\n" + e.getMessage());
                }
                int murderSlot = Integer.parseInt(Config.readConfig(configKey, "murder_weapon_slot"));
                playerList.get(a).getInventory().setItem(murderSlot, new ItemStack(murderWeapon));
                murder = playerList.get(a).getName();
            }
            if (a == detectiveRandom)
            {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clear " + playerList.get(a).getPlayer().getName());
                int detectiveSlot = Integer.parseInt(Config.readConfig(configKey, "detective_weapon_slot"));
                playerList.get(a).getInventory().setItem(detectiveSlot, new ItemStack(Item.getId(Item.getById(261))));
                detective = playerList.get(a).getName();
            }
            if (a != murderRandom && a != detectiveRandom)
            {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clear " + playerList.get(a).getPlayer().getName());
                innocents += playerList.get(a).getName() + ",";
            }
            String playerRole = getRoleString(playerList.get(a).getName());
            String worldName = Config.readConfig("maps", "0", "world_name");
            int playersInWorld = plugin.getServer().getWorld(worldName).getPlayers().size();
            int maxPlayers = Integer.parseInt(Config.readConfig("maps", "0", "max_players"));
            String murderRole = Config.readConfig(configKey, "roles", "murder");
            String roleColor = Config.readConfig(configKey, "roles", playerRole + "_color");
            String roleResult = Config.readConfig(configKey, "roles", playerRole);
            String roleTitle = Config.readConfig(configKey, "role_title").replace("&", "§").replace("{murder_role}", murderRole).replace("{role}", roleResult).replace("{max_players}", String.valueOf(maxPlayers)).replace("{players_count}", String.valueOf(playersInWorld)).replace("{nick}", playerList.get(a).getName()).replace("{role_color}", roleColor);
            String roleSubtitle = Config.readConfig(configKey, "roles", playerRole + "_subtitle").replace("{murder_role}", murderRole).replace("{role}", roleResult).replace("{max_players}", String.valueOf(maxPlayers)).replace("{players_count}", String.valueOf(playersInWorld)).replace("{nick}", playerList.get(a).getName()).replace("{role_color}", roleColor);
            String roleWarning = Config.readConfig(configKey, "roles", playerRole + "_warning").replace("{murder_role}", murderRole).replace("{role}", roleResult).replace("{max_players}", String.valueOf(maxPlayers)).replace("{players_count}", String.valueOf(playersInWorld)).replace("{nick}", playerList.get(a).getName()).replace("{role_color}", roleColor);
            playerList.get(a).sendTitle(roleTitle, roleSubtitle);
            playerList.get(a).sendMessage(roleWarning);
            a++;
        }
    }
    public static void stopGame(Boolean innocentsWin) throws IOException, InvalidConfigurationException {
        murder = ""; detective = ""; innocents = ""; ghosts = "";
        if (innocentsWin)
        {
            String winInnocentMessage = Config.readConfig(configKey, "innocent_win_message");
            plugin.getServer().broadcastMessage(winInnocentMessage);
        }
        else
        {
            String winMurderMessage = Config.readConfig(configKey, "murder_win_message");
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
    public static String getRoleString(String nick)
    {
        Player player = Bukkit.getPlayer(nick);
        int playerRoleInt = getRole(player.getName());
        if (playerRoleInt == 0)
        {
            return "ghost";
        }
        if (playerRoleInt == 1)
        {
            return "innocent";
        }
        if (playerRoleInt == 2)
        {
            return "detective";
        }
        if (playerRoleInt == 3)
        {
            return "murder";
        }
        return "";
    }
    public static void playerDeath(String nick) throws IOException, InvalidConfigurationException {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clear " + nick);
        innocents = innocents.replace(nick + ",", "");
        ghosts += nick + ",";
        Player player = Bukkit.getPlayer(nick);
        player.setAllowFlight(true);
        player.setFlying(true);
        playSound(player, Sound.SUCCESSFUL_HIT);
        String deathTitle = Config.readConfig(configKey, "death_title");
        String deathSubTitle = Config.readConfig(configKey, "death_subtitle");
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
}
