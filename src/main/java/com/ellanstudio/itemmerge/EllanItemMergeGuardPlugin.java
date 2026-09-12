package com.ellanstudio.itemmerge;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class EllanItemMergeGuardPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    private volatile boolean enabled;
    private volatile Set<Material> protectedMaterials = EnumSet.noneOf(Material.class);
    private volatile Set<String> worlds = Set.of();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadGuard();
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("ellanitemmerge") != null) {
            getCommand("ellanitemmerge").setExecutor(this);
            getCommand("ellanitemmerge").setTabCompleter(this);
        }
        getLogger().info("EllanItemMergeGuard enabled.");
    }

    private void reloadGuard() {
        reloadConfig();
        enabled = getConfig().getBoolean("enabled", true);
        worlds = getConfig().getStringList("worlds").stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());

        EnumSet<Material> materials = EnumSet.noneOf(Material.class);
        for (String value : getConfig().getStringList("protected-materials")) {
            Material material = Material.matchMaterial(value);
            if (material == null || material.isAir()) {
                getLogger().warning("Unknown protected material: " + value);
                continue;
            }
            materials.add(material);
        }
        protectedMaterials = materials;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent event) {
        if (!enabled || protectedMaterials.isEmpty()) {
            return;
        }
        Item source = event.getEntity();
        Item target = event.getTarget();
        if (!worlds.isEmpty() && !worlds.contains(source.getWorld().getName().toLowerCase(Locale.ROOT))) {
            return;
        }
        if (protectedMaterials.contains(source.getItemStack().getType())
                || protectedMaterials.contains(target.getItemStack().getType())) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ellanitemmerge.admin")) {
            sender.sendMessage("§c你没有权限执行该命令。");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§e/" + label + " <reload|status>");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            reloadGuard();
            sender.sendMessage("§aEllanItemMergeGuard 配置已重新加载，共保护 "
                    + protectedMaterials.size() + " 种材料。");
            return true;
        }
        if (args[0].equalsIgnoreCase("status")) {
            sender.sendMessage("§7enabled=" + enabled
                    + ", materials=" + protectedMaterials.size()
                    + ", worlds=" + (worlds.isEmpty() ? "ALL" : worlds));
            return true;
        }
        sender.sendMessage("§e/" + label + " <reload|status>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return List.of("reload", "status").stream()
                    .filter(value -> value.startsWith(prefix))
                    .toList();
        }
        return List.of();
    }
}
