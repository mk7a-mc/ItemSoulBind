package com.mk7a.soulbind.commands;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.main.PluginPermissions;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class UnbindCommand implements CommandExecutor {


    private final ItemSoulBindPlugin plugin;
    private final CommandsModule module;
    private final PluginConfiguration config;

    UnbindCommand(ItemSoulBindPlugin plugin, CommandsModule module) {
        this.plugin = plugin;
        this.module = module;
        this.config = ItemSoulBindPlugin.getPluginConfig();
    }

    public void register() {
        plugin.getCommand(CommandsModule.UNBIND_ITEM).setExecutor(this);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;


        if (!player.hasPermission(PluginPermissions.UNBIND)) {
            Util.sendMessage(player, config.noPermissionGeneric);
            return true;
        }

        if (module.mainHandEmpty(player)) {
            Util.sendMessage(player, config.bindErrorHeldItem);
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!BindUtil.hasOwner(item)) {
            Util.sendMessage(player, config.unbindErrorNotBound);
            return true;
        }

        cleanLore(item);

        ItemStack newItem = BindUtil.removeOwner(item);
        player.getInventory().setItemInMainHand(newItem);
        player.sendMessage(config.prefix + config.unbindSuccess);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);


        return true;
    }

    private void cleanLore(ItemStack item) {

        ItemMeta meta = item.getItemMeta();

        List<String> lore = meta.getLore();

        if (lore != null)  {

            for (String line : lore) {
                if (line.startsWith(config.loreMsgStart)) {
                    lore.remove(line);
                    break;
                }
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }


}
