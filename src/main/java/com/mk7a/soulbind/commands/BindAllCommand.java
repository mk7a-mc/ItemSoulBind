package com.mk7a.soulbind.commands;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BindAllCommand implements CommandExecutor {

    private final ItemSoulBindPlugin plugin;
    private final CommandsModule module;
    private final PluginConfiguration config;

    BindAllCommand(ItemSoulBindPlugin plugin, CommandsModule module) {
        this.plugin = plugin;
        this.module = module;
        this.config = ItemSoulBindPlugin.getPluginConfig();
    }

    public void register() {
        plugin.getCommand(CommandsModule.BIND_ALL).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return false;
        }

        module.bindAllInvItems(player);

        Util.sendMessage(player, config.bindSuccess);

        return true;
    }
}
