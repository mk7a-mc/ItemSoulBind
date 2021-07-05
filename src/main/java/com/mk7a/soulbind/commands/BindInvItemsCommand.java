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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
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

        if (!sender.hasPermission(PluginPermissions.BIND_INV_ITEMS)) {
            Util.sendMessage(sender, config.noPermissionGeneric);
            return true;
        }

        if (args.length != 1) {
            return false;
        }

        String targetPlayerName = args[0];
        Optional<Player> getTargetPlayer = module.findPlayerFromName(targetPlayerName);

        if (getTargetPlayer.isEmpty()) {
            Util.sendMessage(sender, config.bindErrorNoSuchPlayer);
            return true;
        }

        Player targetPlayer = getTargetPlayer.get();
        ItemStack[] invContents = targetPlayer.getInventory().getContents();

        for (int i = 0; i < invContents.length; i++) {

            if (!isBindCandidate(invContents[i])) {
                continue;
            }

            ItemStack currentItem = invContents[i];
            ItemMeta meta = currentItem.getItemMeta();
            List<String> lore = meta.getLore();

            for (String line : lore) {

                if (line.contains(config.registerString)) {

                    lore.remove(line);

                    if (config.displayLore) {
                        lore.add(config.loreMsg.replaceAll(CommandsModule.USERNAME_PLACEHOLDER, targetPlayer.getName()));
                    }

                    meta.setLore(lore);
                    currentItem.setItemMeta(meta);
                    ItemStack regItem = BindUtil.setOwner(currentItem, targetPlayer);

                    invContents[i] = regItem;
                    break;
                }
            }

        }

        Util.sendMessage(sender, config.inventoryProcessSuccess + targetPlayerName);

        return true;
    }


    private boolean isBindCandidate(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasLore();
    }


}
