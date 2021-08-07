package ru.owopeef.owomurdermystery;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@SuppressWarnings("deprecation")
public class PlayerEvents implements Listener
{
    Plugin plugin = JavaPlugin.getPlugin(Main.class);
    String donatePrefix;
    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clear " + player.getName());
        donatePrefix = "§7";
        float startPosX = Float.parseFloat(readConfig("maps", "0.start_pos_x"));
        float startPosY = Float.parseFloat(readConfig("maps", "0.start_pos_y"));
        float startPosZ = Float.parseFloat(readConfig("maps", "0.start_pos_z"));
        String worldName = readConfig("maps", "0.world_name");
        int playersInWorld = plugin.getServer().getWorld(worldName).getPlayers().size() + 1;
        int maxPlayers = Integer.parseInt(readConfig("maps", "0.max_players"));
        String joinMessage = readConfig("maps", "0.join_message").replace("{donate_prefix}", donatePrefix).replace("{nick}", player.getName()).replace("{players_count}", String.valueOf(playersInWorld)).replace("{max_players}", String.valueOf(maxPlayers));
        event.getPlayer().teleport(new Location(event.getPlayer().getWorld(), startPosX, startPosY, startPosZ));
        player.setPlayerListName(donatePrefix + player.getName());
        event.setJoinMessage(joinMessage);
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clear " + player.getName());
        donatePrefix = "§7";
        String worldName = readConfig("maps", "0.world_name");
        int playersInWorld = plugin.getServer().getWorld(worldName).getPlayers().size();
        int maxPlayers = Integer.parseInt(readConfig("maps", "0.max_players"));
        String quitMessage = readConfig("maps", "0.quit_message").replace("{donate_prefix}", donatePrefix).replace("{nick}", player.getName()).replace("{players_count}", String.valueOf(playersInWorld)).replace("{max_players}", String.valueOf(maxPlayers));
        event.setQuitMessage(quitMessage);
    }
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event)
    {
        event.setCancelled(true);
    }
    @EventHandler
    public void onFloorDamage(EntityDamageEvent event)
    {
        if (Objects.equals(event.getEventName(), "EntityDamageEvent"))
        {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event)
    {
        if (Objects.equals(event.getEventName(), "EntityDamageByEntityEvent"))
        {
            Material damageEntityHandItem;
            try
            {
                damageEntityHandItem = event.getDamager().getServer().getPlayer(event.getDamager().getName()).getItemInHand().getType();
            }
            catch (Exception e)
            {
                damageEntityHandItem = Material.BOW;
            }
            event.setCancelled(true);
            // ROLES TYPE:
            // 0 - GHOST
            // 1 - INNOCENT
            // 2 - DETECTIVE
            // 3 - MURDER
            Material murderWeapon = Material.IRON_SWORD;
            try
            {
                murderWeapon = Material.getMaterial(readConfig("maps", "0.murder_weapon").toUpperCase());
            }
            catch (Exception e)
            {
                plugin.getLogger().warning("Material not found!\n" + e.getMessage());
            }
            if (damageEntityHandItem == Material.BOW)
            {
                plugin.getLogger().info("BOW MATERIAL");
                Arrow arrow = (Arrow) event.getDamager();
                Player shooter = (Player) arrow.getShooter();
                int role = MurderMysteryManager.getRole(String.valueOf(shooter));
                arrow.remove();
                plugin.getLogger().info("Role: " + role);
                if (role == 2)
                {
                    plugin.getLogger().info("if role == 2");
                    role = MurderMysteryManager.getRole(event.getEntity().getName());
                    if (role == 3)
                    {
                        plugin.getLogger().info("if role == 3");
                        MurderMysteryManager.playerDeath(event.getEntity().getName());
                    }
                }
            }
            if (damageEntityHandItem == murderWeapon)
            {
                int role = MurderMysteryManager.getRole(event.getDamager().getName());
                if (role == 3)
                {
                    int o = 0;
                    String worldName = readConfig("maps", "0.world_name");
                    List<Player> playerList = plugin.getServer().getWorld(worldName).getPlayers();
                    while (o != playerList.size())
                    {
                        if (Objects.equals(playerList.get(o).getName(), event.getEntity().getName()))
                        {
                            MurderMysteryManager.playerDeath(event.getEntity().getName());
                        }
                        else
                        {
                            playerList.get(o).hidePlayer((Player) event.getEntity());
                        }
                        o++;
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPickup(PlayerPickupItemEvent event)
    {
        if (event.getItem().getItemStack().getData().getItemType() == Material.GOLD_INGOT)
        {
            event.getItem().remove();
            Player player = event.getPlayer();
            player.getInventory().setItem(0, new ItemStack(266));
            plugin.getServer().getLogger().info("Pickup gold ingot");
        }
    }
    public String readConfig(String path, String parent)
    {
        return plugin.getConfig().get(path + "." + parent).toString();
    }
}
