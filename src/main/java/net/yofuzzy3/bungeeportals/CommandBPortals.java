package net.yofuzzy3.bungeeportals;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CommandBPortals implements CommandExecutor {

    private BungeePortals plugin;

    public CommandBPortals(BungeePortals plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!sender.hasPermission("bungeeportals.command.bportals")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }
        if (args.length == 0) {
            help(sender);
            return true;
        }
        List<String> blocks;
        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.loadConfigFiles();
                plugin.loadPortalsData();
                sender.sendMessage(ChatColor.GREEN + "All configuration files and data have been reloaded.");
                break;
            case "create":
                blocks = select(player, ((args.length >= 3) ? args[2] : null));
                if (blocks != null) {
                    for (String block : blocks) {
                        plugin.getPortalData().put(block, args[1]);
                    }
                    player.sendMessage(ChatColor.GREEN + "" + blocks.size() + " portals have been created.");
                    plugin.savePortalsData();
                }
                break;
            case "remove":
                blocks = select(player, null);
                if (blocks != null) {
                    int count = 0;
                    for (String block : blocks) {
                        if (plugin.getPortalData().containsKey(block)) {
                            plugin.getPortalData().remove(block);
                            count++;
                        }
                    }
                    sender.sendMessage(ChatColor.GREEN + "" + count + " portal blocks have been removed.");
                    plugin.savePortalsData();
                }
                break;
            default:
                help(sender);
        }
        return true;
    }

    private void help(CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "BungeePortals v" + plugin.getDescription().getVersion() + " by YoFuzzy3");
        sender.sendMessage(ChatColor.GREEN + "/BPortals reload " + ChatColor.RED + "Reload all files and data.");
        sender.sendMessage(ChatColor.GREEN + "/BPortals create <destination> <filter,list> " + ChatColor.RED + "Create portals.");
        sender.sendMessage(ChatColor.GREEN + "/BPortals remove " + ChatColor.RED + "Remove portals.");
        sender.sendMessage(ChatColor.BLUE + "Visit www.spigotmc.org/resources/bungeeportals.19 for help.");
    }

    private List<String> select(Player player, String args) {

        LocalSession userSession = plugin.getWorldEdit().getSession(player);
        World world = BukkitAdapter.adapt(player.getWorld());
        Region selection;

        try {
            if (userSession != null && userSession.getRegionSelector(world).isDefined()) {
                selection = userSession.getSelection(world);
            } else {
                player.sendMessage(ChatColor.RED + "You have to first create a WorldEdit selection!");
                return null;
            }
        } catch (IncompleteRegionException ex) {
            player.sendMessage(ChatColor.RED + "You have to first create a WorldEdit selection!");
            return null;
        }

        if (selection == null) {
            player.sendMessage(ChatColor.RED + "You have to first create a WorldEdit selection!");
            return null;
        }
        if (!(selection instanceof CuboidRegion)) {
            player.sendMessage(ChatColor.RED + "Must be a cuboid selection!");
            return null;
        }
        List<Location> locations = getLocationsFromCuboid(selection);

        List<String> filterIds = new ArrayList<>();
        if (args != null) {
            filterIds = Arrays.asList(args.split(","));
            filterIds.replaceAll(String::toUpperCase);
        }

        List<String> blocks = new ArrayList<>();
        for (Location location : locations) {
            Block block = player.getWorld().getBlockAt(location);
            if(!filterIds.contains(String.valueOf(block.getType().name()))
                    && !filterIds.contains(block.getType().name())) {
                continue;
            }
            blocks.add(block.getWorld().getName() + "#" + block.getX() + "#" + block.getY() + "#" + block.getZ());
        }
        player.sendMessage(ChatColor.GREEN + "" + locations.size() + " blocks have been selected, " + blocks.size() + " filtered.");
        return blocks;
    }

    // TODO: Move to utils
    private List<Location> getLocationsFromCuboid(Region cuboid) {
        List<Location> locations = new ArrayList<>();
        BlockVector3 minLoc = cuboid.getMinimumPoint();
        BlockVector3 maxLoc = cuboid.getMaximumPoint();
        for (int i1 = minLoc.getBlockX(); i1 <= maxLoc.getBlockX(); i1++) {
            for (int i2 = minLoc.getBlockY(); i2 <= maxLoc.getBlockY(); i2++) {
                for (int i3 = minLoc.getBlockZ(); i3 <= maxLoc.getBlockZ(); i3++) {
                    locations.add(new Location(BukkitAdapter.adapt(cuboid.getWorld()), i1, i2, i3));
                }
            }
        }
        return locations;
    }
}
