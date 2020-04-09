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

import java.util.Optional;

public class BindCommand implements CommandExecutor {


    private final ItemSoulBindPlugin plugin;
    private final CommandsModule module;
    private final PluginConfiguration config;

    BindCommand(ItemSoulBindPlugin plugin, CommandsModule module) {
        this.plugin = plugin;
        this.module = module;
        this.config = ItemSoulBindPlugin.getPluginConfig();
    }

    public void register() {
        plugin.getCommand(CommandsModule.BIND_ITEM).setExecutor(this);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(PluginPermissions.BIND)) {
            Util.sendMessage(player, config.noPermissionGeneric);
            return true;
        }

        if (module.mainHandEmpty(player)) {
            Util.sendMessage(player, config.bindErrorHeldItem);
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (BindUtil.hasOwner(item)) {
            Util.sendMessage(player, config.bindErrorAlreadyBound);
            return true;
        }

        boolean bindOnSelf = args.length == 0;
        boolean bindToPlayer = args.length == 1;

        if (bindOnSelf) {

            module.bindItem(item, player, player);
            Util.bindEffect(player);
            return true;

        } else if (bindToPlayer) {

            Optional<Player> targetPlayer = module.findPlayerFromName(args[0]);

            if (!targetPlayer.isPresent()) {
                Util.sendMessage(player, config.bindErrorNoSuchPlayer);
                return true;
            }

            if (!player.hasPermission(PluginPermissions.BIND_OTHERS)) {
                Util.sendMessage(player, config.noPermissionBindOthers);
                return true;
            }

            module.bindItem(item, targetPlayer.get(), player);
            Util.bindEffect(player);
            return true;
        }


        return false;
    }



}
