package com.mk7a.soulbind.commands;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SpecialBindCommands implements CommandExecutor {


    private final ItemSoulBindPlugin plugin;
    private final CommandsModule module;
    private final PluginConfiguration config;


    SpecialBindCommands(ItemSoulBindPlugin plugin, CommandsModule module) {
        this.plugin = plugin;
        this.module = module;
        this.config = ItemSoulBindPlugin.getPluginConfig();
    }


    public void register() {
        plugin.getCommand(CommandsModule.BIND_ON_USE).setExecutor(this);
        plugin.getCommand(CommandsModule.BIND_ON_EQUIP).setExecutor(this);
        plugin.getCommand(CommandsModule.BIND_ON_PICKUP).setExecutor(this);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (module.mainHandEmpty(player)) {
            Util.sendMessage(player, config.bindErrorHeldItem);
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (BindUtil.hasOwner(item)) {
            Util.sendMessage(player, config.bindErrorAlreadyBound);
        }

        ItemMeta meta = addSpecialBindLoreString(item.getItemMeta(), command.getName().toLowerCase());
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);

        Util.sendMessage(player, config.specialBindDone);

        return true;
    }


    private ItemMeta addSpecialBindLoreString(ItemMeta meta, String command) {

        List<String> lore = new ArrayList<>();

        if (meta.hasLore()) {
            lore.addAll(meta.getLore());
        }

        if (command.equalsIgnoreCase(CommandsModule.BIND_ON_USE)) {
            lore.add(config.bindOnUseString);

        } else if (command.equalsIgnoreCase(CommandsModule.BIND_ON_EQUIP)) {
            lore.add(config.bindOnEquipString);

        } else if (command.equalsIgnoreCase(CommandsModule.BIND_ON_PICKUP)) {
            lore.add(config.bindOnPickupString);
        }

        meta.setLore(lore);

        return meta;
    }

}
