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
import org.bukkit.scoreboard.*;
import ru.owopeef.owomurdermystery.utils.Config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class MurderMysteryManager
{
    static Plugin plugin = JavaPlugin.getPlugin(Main.class);
    public static String configKey = Config.configKey;
    // Game Statuses
    //  0   -   game not started
    //  1   -   game started
    //  2   -   game ended
    public static int gameStatus0 = 0;
    public static int minutes = 3;
    public static int seconds = 0;
    public static String murder, detective, innocents, ghosts = "";
    // THREADS
    // update
    public static Thread update = new Thread(() -> {
        while (true)
        {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                try {
                    String title = Config.readConfig("settings", "scoreboard", "started", "title");
                    List<String> messages = Config.readConfigStringList("settings", "scoreboard", "started", "lines");
                    scoreboardSet(title, messages);
                } catch (Exception e) {
                    plugin.getLogger().info(e.getLocalizedMessage());
                }
            }, 20L);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
    // timer_th
    public static Thread timer_th = new Thread(() -> {
        while(true)
        {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (minutes == 0 && seconds == 0)
                {
                    try {
                        stopGame(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (seconds == 0)
                {
                    minutes--;
                    seconds = 59;
                }
                else
                {
                    seconds--;
                }
            }, 20L);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
    // th
    public static Thread th = new Thread(() -> {
        while(true)
        {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                String[] split = new String[0];
                try {
                    split = Config.readConfigString("maps", "0", "gold").split(";");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int randomInt = (int) (Math.random() * split.length - 1);
                String[] cords = split[randomInt].split(",");
                World w = Bukkit.getWorlds().get(0);
                float x = Float.parseFloat(cords[0]);
                float y = Float.parseFloat(cords[1]);
                float z = Float.parseFloat(cords[2]);
                Location loc = new Location(w, x, y, z);
                ItemStack gold = new ItemStack(266);
                Bukkit.getWorlds().get(0).dropItemNaturally(loc, gold);
            }, 20L);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
    // END THREADS
    public static void startGame() throws IOException, InvalidConfigurationException
    {
        try {
            String title = Config.readConfig("settings", "scoreboard", "started", "title");
            List<String> messages = Config.readConfigStringList("settings", "scoreboard", "started", "lines");
            scoreboardSet(title, messages);
            timerStart();
            scoreboardUpdate();
        } catch (Exception e) {
            plugin.getLogger().info(e.getLocalizedMessage());
        }
        gameStatus0 = 1;
        murder = ""; detective = ""; innocents = ""; ghosts = "";
        List<Player> playerList = plugin.getServer().getWorld(plugin.getServer().getWorlds().get(0).getName()).getPlayers();
        int playerSize = playerList.size();
        startGoldSpawn();
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
                playerList.get(a).getInventory().setItem(10, new ItemStack(Item.getId(Item.getById(262))));
                int t = 0;
                while (t != 62)
                {
                    playerList.get(a).getInventory().addItem(new ItemStack(Item.getId(Item.getById(262))));
                    t++;
                }
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
        gameStatus0 = 2;
        timerStop();
        scoreboardStop();
        stopGoldSpawn();
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
        String title = Config.readConfig("settings", "scoreboard", "started", "title");
        List<String> messages = Config.readConfigStringList("settings", "scoreboard", "started", "lines");
        scoreboardSet(title, messages);
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
    public static void scoreboardSet(String title, List<String> messages) throws IOException, InvalidConfigurationException {
        String map_name = "map";
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        final Scoreboard board = manager.getNewScoreboard();
        final Objective objective = board.registerNewObjective("murder_mystery", "scoreboard");
        List<Player> players = plugin.getServer().getWorld(plugin.getServer().getWorlds().get(0).getName()).getPlayers();
        int d = 0;
        int players_size = players.size();
        int players_count = 0;
        boolean potionFounded;
        int role = 0;
        while (d != players_size)
        {
            potionFounded = false;
            Collection<PotionEffect> pe = players.get(d).getActivePotionEffects();
            ArrayList<PotionEffect> potionEffects = new ArrayList<>(pe);
            int potions_size = potionEffects.size();
            int c = 0;
            while (c != potions_size)
            {
                PotionEffect currentEffect = potionEffects.get(c);
                if (Objects.equals(currentEffect.getType().getName(), "INVISIBILITY"))
                {
                    potionFounded = true;
                }
                c++;
            }
            if (!potionFounded)
            {
                players_count++;
            }
            role = getRole(players.get(d).getName());
            d++;
        }
        int max_players = Integer.parseInt(Config.readConfig("maps", "0", "max_players"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(title);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yy");
        LocalDateTime now = LocalDateTime.now();
        int a = 0;
        int scoreCount = messages.size();
        while (a != messages.size())
        {
            assert false;
            String time = "";
            if (String.valueOf(seconds).length() == 1)
            {
                time = "0" + minutes + ":0" + seconds;
            }
            if (String.valueOf(seconds).length() == 2)
            {
                time = "0" + minutes + ":" + seconds;
            }
            String currentMessage = messages.get(a).replace("{now}", dtf.format(now)).replace("{map_name}", map_name).replace("{players_count}", String.valueOf(players_count - 1)).replace("{max_players}", String.valueOf(max_players)).replace("{time}", time);
            if (role == 0)
            {
                currentMessage = currentMessage.replace("{role}", "§7Призрак");
            }
            if (role == 1)
            {
                currentMessage = currentMessage.replace("{role}", "§aМирный житель");
            }
            if (role == 2)
            {
                currentMessage = currentMessage.replace("{role}", "§bДетектив");
            }
            if (role == 3)
            {
                currentMessage = currentMessage.replace("{role}", "§cУбийца");
            }
            if (currentMessage.length() == 0)
            {
                int b = 0;
                while (b != a)
                {
                    currentMessage += " ";
                    b++;
                }
            }
            Score e = objective.getScore(currentMessage);
            e.setScore(scoreCount);
            scoreCount--;
            a++;
        }
        int o = 0;
        while (o != players_size)
        {
            players.get(o).setScoreboard(board);
            o++;
        }
    }
    public static void scoreboardUpdate()
    {
        update.start();
    }
    public static void scoreboardStop()
    {
        update.stop();
    }
    public static void timerStart()
    {
        timer_th.start();
    }
    public static void timerStop()
    {
        timer_th.stop();
    }
    public static void startGoldSpawn()
    {
        th.start();
    }
    public static void stopGoldSpawn()
    {
        th.stop();
    }
    public static Integer gameStatus()
    {
        return gameStatus0;
    }
    public static void playSound(Player player, Sound sound)
    {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }
}
