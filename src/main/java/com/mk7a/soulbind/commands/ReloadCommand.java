package com.mk7a.soulbind.commands;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class ReloadCommand implements CommandExecutor {


    private final ItemSoulBindPlugin plugin;

    ReloadCommand(ItemSoulBindPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getCommand(CommandsModule.ISB_RELOAD).setExecutor(this);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        plugin.reloadPluginConfig();
        Util.sendMessage(sender, "Configuration Reloaded.");

        return true;
    }


}
