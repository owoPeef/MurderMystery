package ru.owopeef.owomurdermystery;

import org.bukkit.plugin.java.JavaPlugin;
import ru.owopeef.owomurdermystery.commands.Commands;

import java.io.File;

public class Main extends JavaPlugin
{
    // TODO
    // Control panel plugin
    public static String configKey = "settings";
    @Override
    public void onEnable()
    {
        loadConfig();
        getCommand("murder_mystery").setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
    }
    public void loadConfig() {
        File currentFile = new File(System.getProperty("user.dir") + "\\plugins\\owoMurderMystery\\config.yml");
        if (!currentFile.exists())
        {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
    }
}
