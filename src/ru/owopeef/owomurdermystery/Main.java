package ru.owopeef.owomurdermystery;

import org.bukkit.plugin.java.JavaPlugin;
import ru.owopeef.owomurdermystery.commands.Commands;
import ru.owopeef.owomurdermystery.utils.Config;

public class Main extends JavaPlugin
{
    // TODO
    // Control Panel Plugin
    // &&&
    // Chat Manager Plugin
    @Override
    public void onEnable()
    {
        Config.loadConfig();
        getCommand("murder_mystery").setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
    }
}
