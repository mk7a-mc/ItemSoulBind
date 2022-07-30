package com.mk7a.soulbind.commands;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.main.PluginPermissions;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GroupBindCommand implements CommandExecutor {


    private final ItemSoulBindPlugin plugin;
    private final CommandsModule module;
    private final PluginConfiguration config;

    GroupBindCommand(ItemSoulBindPlugin plugin, CommandsModule module) {
        this.plugin = plugin;
        this.module = module;
        this.config = ItemSoulBindPlugin.getPluginConfig();
    }

    public void register() {
        plugin.getCommand(CommandsModule.GROUP_BIND_ITEM).setExecutor(this);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return false;
        }

        if (!player.hasPermission(PluginPermissions.GROUP_BIND)) {
            Util.sendMessage(player, config.noPermissionGeneric);
            return true;
        }

        if (args.length < 1) {
            return false;
        }

        if (module.mainHandEmpty(player)) {
            Util.sendMessage(player, config.bindErrorHeldItem);
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (BindUtil.hasBind(item)) {
            Util.sendMessage(player, config.bindErrorAlreadyBound);
            return true;
        }

        module.bindItemToGroupPerm(item, args[0], player);

        return true;
    }





}
