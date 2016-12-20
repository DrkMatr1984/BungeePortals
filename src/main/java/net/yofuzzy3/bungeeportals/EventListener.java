package net.yofuzzy3.bungeeportals;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class EventListener implements Listener {

    private BungeePortals plugin;
    private HashMap<Player, Long> cooldown;

    public EventListener(BungeePortals plugin) {
        this.plugin = plugin;
        cooldown = new HashMap<>();
    }

    private boolean checkCooldown(Player player) {
        if (!cooldown.containsKey(player)) {
            return false;
        }

        final int delay = plugin.getConfig().getInt("CooldownSeconds");
        long diff = ((System.currentTimeMillis() - cooldown.get(player)) / 1000);
        if (diff < delay) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.configFile.getString("CooldownMessage")
                            .replace("{destination}", String.valueOf(delay - diff))));
            return true;
        }
        return false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Cleanup to prevent a memory leak
        cooldown.remove(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) throws IOException {
        Player player = event.getPlayer();
        if (checkCooldown(player)) {
            return;
        }

        Block block = player.getWorld().getBlockAt(player.getLocation());
        String data = block.getWorld().getName() + "#" + block.getX() + "#" + block.getY() + "#" + block.getZ();
        if (!plugin.portalData.containsKey(data)) {
            return;
        }

        String destination = plugin.portalData.get(data);
        if (player.hasPermission("bungeeportals.portal." + destination)
                && !player.hasPermission("bungeeportals.portal.deny." + destination)) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bout);
            out.writeUTF("Connect");
            out.writeUTF(destination);
            player.sendPluginMessage(plugin, "BungeeCord", bout.toByteArray());
            bout.close();
            out.close();
            cooldown.put(player, System.currentTimeMillis());
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.configFile.getString("NoPortalPermissionMessage")
                            .replace("{destination}", destination)));
        }

        cooldown.put(player, System.currentTimeMillis());
    }
}
