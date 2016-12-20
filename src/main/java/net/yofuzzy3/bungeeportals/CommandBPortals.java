package net.yofuzzy3.bungeeportals;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
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
        Selection selection = plugin.getWorldEdit().getSelection(player);
        if (selection == null) {
            player.sendMessage(ChatColor.RED + "You have to first create a WorldEdit selection!");
            return null;
        }
        if (!(selection instanceof CuboidSelection)) {
            player.sendMessage(ChatColor.RED + "Must be a cuboid selection!");
            return null;
        }
        List<Location> locations = getLocationsFromCuboid((CuboidSelection) selection);

        List<String> filterIds = new ArrayList<>();
        if (args != null) {
            filterIds = Arrays.asList(args.split(","));
        }

        List<String> blocks = new ArrayList<>();
        for (Location location : locations) {
            Block block = player.getWorld().getBlockAt(location);
            if(!filterIds.contains(String.valueOf(block.getTypeId()))
                    && !filterIds.contains(block.getTypeId() + ":" + block.getData())
                    && !filterIds.contains(block.getType().name())) {
                continue;
            }
            blocks.add(block.getWorld().getName() + "#" + block.getX() + "#" + block.getY() + "#" + block.getZ());
        }
        player.sendMessage(ChatColor.GREEN + "" + locations.size() + " blocks have been selected, " + blocks.size() + " filtered.");
        return blocks;
    }

    // TODO: Move to utils
    private List<Location> getLocationsFromCuboid(CuboidSelection cuboid) {
        List<Location> locations = new ArrayList<>();
        Location minLocation = cuboid.getMinimumPoint();
        Location maxLocation = cuboid.getMaximumPoint();
        for (int i1 = minLocation.getBlockX(); i1 <= maxLocation.getBlockX(); i1++) {
            for (int i2 = minLocation.getBlockY(); i2 <= maxLocation.getBlockY(); i2++) {
                for (int i3 = minLocation.getBlockZ(); i3 <= maxLocation.getBlockZ(); i3++) {
                    locations.add(new Location(cuboid.getWorld(), i1, i2, i3));
                }
            }
        }
        return locations;
    }
}
