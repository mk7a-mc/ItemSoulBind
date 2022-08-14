package com.mk7a.soulbind.commands;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RemoteBindCommand implements CommandExecutor, TabExecutor {


    private final ItemSoulBindPlugin plugin;
    private final CommandsModule module;
    private final PluginConfiguration config;

    private static final String ARG_MAINHAND = "mainhand";
    private static final String ARG_OFFHAND = "offhand";
    private static final String ARG_HELMET = "helmet";
    private static final String ARG_CHESTPLATE = "chestplate";
    private static final String ARG_LEGGINGS = "leggings";
    private static final String ARG_BOOTS = "boots";
    private static final String ARG_ALL = "all";
    private static final int OFFHAND_SLOT = 40;
    private static final int BOOTS_SLOT = 36;
    private static final int LEGGINGS_SLOT = 37;
    private static final int CHESTPLATE_SLOT = 38;
    private static final int HELMET_SLOT = 39;
    private static final int SLOT_MIN = 0;
    private static final int SLOT_MAX = 40;

    RemoteBindCommand(ItemSoulBindPlugin plugin, CommandsModule module) {
        this.plugin = plugin;
        this.module = module;
        this.config = ItemSoulBindPlugin.getPluginConfig();
    }

    public void register() {
        plugin.getCommand(CommandsModule.REMOTE_BIND).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 2) {
            return false;
        }

        Optional<Player> targetPlayer = module.findOnlinePlayerFromName(args[0]);

        if (targetPlayer.isEmpty()) {
            Util.sendMessage(sender, config.bindErrorNoSuchPlayer);
            return true;
        }

        Player player = targetPlayer.get();

        if (args[1].equals(ARG_ALL)) {
            module.bindAllInvItems(player);

        } else {

            int slot;

            switch (args[1]) {
                case ARG_MAINHAND -> slot = player.getInventory().getHeldItemSlot();
                case ARG_OFFHAND -> slot = OFFHAND_SLOT;
                case ARG_BOOTS -> slot = BOOTS_SLOT;
                case ARG_LEGGINGS -> slot = LEGGINGS_SLOT;
                case ARG_CHESTPLATE -> slot = CHESTPLATE_SLOT;
                case ARG_HELMET -> slot = HELMET_SLOT;
                default -> {
                    try {
                        slot = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }

            if (slot < SLOT_MIN || slot > SLOT_MAX) {
                return false;
            }

            Optional<ItemStack> item = Optional.ofNullable(player.getInventory().getItem(slot));

            if (item.isEmpty() || item.get().getType().equals(Material.AIR)) {
                Util.sendMessage(sender, config.bindErrorRemoteNoItem);
                return true;

            } else if (BindUtil.hasBind(item.get())) {
                Util.sendMessage(sender, config.bindErrorAlreadyBound);
                return true;

            } else {
                module.bindItemToPlayer(item.get(), player, player, false);
            }
        }

        Util.sendMessage(sender, config.bindSuccess);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());

        } else if (args.length == 2) {
            return List.of(ARG_MAINHAND, ARG_OFFHAND, ARG_BOOTS, ARG_LEGGINGS, ARG_CHESTPLATE, ARG_HELMET, ARG_ALL);

        } else {
            return List.of();
        }
    }
}