package com.mk7a.soulbind.listeners;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class CommandBlockerListener implements Listener {

    private final ItemSoulBindPlugin plugin;
    private final PluginConfiguration config;

    public CommandBlockerListener(ItemSoulBindPlugin plugin) {
        this.plugin = plugin;
        config = ItemSoulBindPlugin.getPluginConfig();
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void preCommand(PlayerCommandPreprocessEvent event) {

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();

        if (isCommandBlocked(event.getMessage())) {

            ItemStack mainHandItem = inventory.getItemInMainHand();

            if (!mainHandItem.getType().equals(Material.AIR)) {


                if (BindUtil.hasBind(mainHandItem)) {

                    event.setCancelled(true);
                    Util.sendMessage(player, config.cmdBlocked);
                }
            }

        }
    }

    private boolean isCommandBlocked(String fullCommand) {

        String command = fullCommand.substring(1);

        String mainCommand = command.split(" ")[0];
        boolean simpleCheck = config.blockedCommands.contains(mainCommand);

        boolean advancedCheck = false;

        for (String blocked : config.blockedCommands) {
            if (command.toLowerCase().startsWith(blocked.toLowerCase())) {
                advancedCheck = true;
                break;
            }
        }

        return simpleCheck || advancedCheck;
    }
}
