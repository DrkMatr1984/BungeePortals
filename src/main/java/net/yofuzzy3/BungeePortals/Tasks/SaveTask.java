package net.yofuzzy3.BungeePortals.Tasks;

import net.yofuzzy3.BungeePortals.BungeePortals;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class SaveTask extends BukkitRunnable {

    private BungeePortals plugin;

    public SaveTask(BungeePortals plugin) {
        this.plugin = plugin;
    }

    public void run() {
    	long time = System.currentTimeMillis();
        if (plugin.configFile.getBoolean("SaveTask.Enabled")) {
        	Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.savePortalsData(plugin.portalsFile, plugin.portalData));
        }
        Bukkit.getLogger().log(Level.INFO, "[BungeePortals] Portal data saved! (" + (System.currentTimeMillis() - time) + "ms)");
    }

}
