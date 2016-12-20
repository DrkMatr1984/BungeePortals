package net.yofuzzy3.bungeeportals;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import fr.xephi.authme.api.NewAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class BungeePortals extends JavaPlugin {

    private Logger logger;

    // Hooks
    private WorldEditPlugin worldEdit;
    private NewAPI authMe;

    private Map<String, String> portalData;
    private FileConfiguration configFile;
    private YamlConfiguration portalsFile;

    public WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }

    public NewAPI getAuthMe() {
        return authMe;
    }

    public Map<String, String> getPortalData() {
        return portalData;
    }

    public FileConfiguration getConfigFile() {
        return configFile;
    }

    @Override
    public void onEnable() {
        logger = getLogger();

        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.getPlugin("WorldEdit") == null) {
            setEnabled(false);
            logger.severe("WorldEdit not found, disabling...");
            return;
        }
        worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");

        // Load config and portals data
        loadConfigFiles();
        portalData = new HashMap<>();
        loadPortalsData();

        // AuthMe hook
        if (configFile.getBoolean("AuthMeHook") && pluginManager.isPluginEnabled("AuthMe")) {
            logger.info("Found AuthMe, trying to hook...");
            try {
                authMe = NewAPI.getInstance();
                logger.info("Hooked into AuthMe!");
            } catch (Exception e) {
                logger.warning("Unable to hook into AuthMe, maybe unsupported version?");
            }
        }

        // Start metrics
        try {
            new MetricsLite(this).start();
            logger.info("Metrics initiated!");
        } catch (IOException e) {
            logger.warning("Unable to initiate metrics.");
            e.printStackTrace();
        }

        // Register command, listeners and plugin channel
        getCommand("BPortals").setExecutor(new CommandBPortals(this));
        pluginManager.registerEvents(new EventListener(this), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        logger.info("Version " + getDescription().getVersion() + " has been enabled.");
    }

    private void createConfigFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfigFiles() {
        // Config file
        saveDefaultConfig();
        configFile = getConfig();
        logger.info("Configuration file loaded!");
        // Portal data file
        File portalFile = new File(getDataFolder(), "portals.yml");
        if (!portalFile.exists()) {
            createConfigFile(getResource("portals.yml"), portalFile);
        }
        portalsFile = YamlConfiguration.loadConfiguration(portalFile);
        logger.info("Portal data file loaded!");
    }

    public void loadPortalsData() {
        try {
            for (String key : portalsFile.getKeys(false)) {
                String value = portalsFile.getString(key);
                portalData.put(key, value);
            }
            logger.info("Portal data loaded!");
        } catch (Exception e) {
            logger.warning("Unable to load portal data!");
            e.printStackTrace();
        }
    }

    public void savePortalsData() {
        for (Entry<String, String> entry : portalData.entrySet()) {
            portalsFile.set(entry.getKey(), entry.getValue());
        }
        try {
            portalsFile.save(new File(getDataFolder(), "portals.yml"));
            logger.info("Portal data saved!");
        } catch (Exception e) {
            logger.warning("Unable to save portal data!");
            e.printStackTrace();
        }
    }
}
