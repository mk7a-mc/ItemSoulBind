package com.mk7a.soulbind.commands;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.util.BindStringUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class BindInvItemsCommand implements CommandExecutor {


    private final ItemSoulBindPlugin plugin;
    private final CommandsModule module;
    private final PluginConfiguration config;

    BindInvItemsCommand(ItemSoulBindPlugin plugin, CommandsModule module) {
        this.plugin = plugin;
        this.module = module;
        this.config = ItemSoulBindPlugin.getPluginConfig();
    }

    public void register() {
        plugin.getCommand(CommandsModule.BIND_INV_ITEMS).setExecutor(this);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 1) {
            return false;
        }

        String targetPlayerName = args[0];
        Optional<Player> getTargetPlayer = module.findOnlinePlayerFromName(targetPlayerName);

        if (getTargetPlayer.isEmpty()) {
            Util.sendMessage(sender, config.bindErrorNoSuchPlayer);
            return true;
        }

        Player targetPlayer = getTargetPlayer.get();
        ItemStack[] invContents = targetPlayer.getInventory().getContents();
        int bindCounter = 0;

        for (int i = 0; i < invContents.length; i++) {

            if (invContents[i] == null) {
                continue;
            }
            ItemStack item = invContents[i];

            Optional<ItemStack> groupBindItem = BindStringUtil.bindIfContainsGroupString(targetPlayer, item);
            if (groupBindItem.isPresent()) {
                invContents[i] = groupBindItem.get();
                bindCounter++;
                continue;
            }

            Optional<ItemStack> nonGroupPickupItem = BindStringUtil.bindIfContainsString(
                    targetPlayer, item, config.registerString, config.bindOnPickupString);
            if (nonGroupPickupItem.isPresent()) {
                invContents[i] = nonGroupPickupItem.get();
                bindCounter++;
            }

        }

        Util.sendMessage(sender, config.inventoryProcessSuccess + targetPlayerName + String.format(" (%d)", bindCounter));

        return true;
    }

}
