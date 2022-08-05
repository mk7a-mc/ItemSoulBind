package com.mk7a.soulbind.commands;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Optional;

public class CommandsModule {

    protected static final String USERNAME_PLACEHOLDER = "%username%";
    protected static final String GROUP_PLACEHOLDER = "%group%";
    protected static final String BIND_ALL = "bindAll";
    protected static final String BIND_ITEM = "bindItem";
    protected static final String GROUP_BIND_ITEM = "groupBindItem";
    protected static final String UNBIND_ITEM = "unbindItem";
    protected static final String BIND_INV_ITEMS = "bindInvItems";
    protected static final String BIND_ON_USE = "bindOnUse";
    protected static final String BIND_ON_EQUIP = "bindOnEquip";
    protected static final String BIND_ON_PICKUP = "bindOnPickup";
    protected static final String ISB_RELOAD = "isb-reload";
    protected static final String ISB = "isb";

    private final ItemSoulBindPlugin plugin;
    private final PluginConfiguration config;


    public CommandsModule(ItemSoulBindPlugin plugin) {
        this.plugin = plugin;
        config = ItemSoulBindPlugin.getPluginConfig();
    }


    public void setup() {
        new BindCommand(plugin, this).register();
        new BindInvItemsCommand(plugin, this).register();
        new ReloadCommand(plugin).register();
        new SpecialBindCommands(plugin, this).register();
        new UnbindCommand(plugin, this).register();
        new BindAllCommand(plugin, this).register();
        new GroupBindCommand(plugin, this).register();
    }


    protected void bindItemToPlayer(ItemStack item, Player targetPlayer, Player itemHolderPlayer, boolean sendBindMsg) {

        if (config.displayLore) {

            ItemMeta meta = item.getItemMeta();
            ArrayList<String> newLore = new ArrayList<>();

            if (meta.hasLore()) {
                newLore.addAll(meta.getLore());
            }

            newLore.add(config.loreMsg.replaceAll(CommandsModule.USERNAME_PLACEHOLDER, targetPlayer.getName()));
            meta.setLore(newLore);
            item.setItemMeta(meta);
        }

        ItemStack soulBoundItem = BindUtil.setPlayerOwner(item, targetPlayer);
        itemHolderPlayer.getInventory().setItem(itemHolderPlayer.getInventory().getHeldItemSlot(), soulBoundItem);
        if (sendBindMsg) {
            Util.sendMessage(itemHolderPlayer, config.bindSuccess);
        }

        Util.bindEffect(itemHolderPlayer);
    }

    protected void bindItemToGroupPerm(ItemStack item, String groupPerm, Player itemHolderPlayer) {

        if (config.displayLoreGroup) {

            ItemMeta meta = item.getItemMeta();
            ArrayList<String> newLore = new ArrayList<>();

            if (meta.hasLore()) {
                newLore.addAll(meta.getLore());
            }

            newLore.add(config.loreMsgGroup.replaceAll(CommandsModule.GROUP_PLACEHOLDER, groupPerm));
            meta.setLore(newLore);
            item.setItemMeta(meta);
        }

        ItemStack soulBoundItem = BindUtil.setGroupOwner(item, groupPerm);
        itemHolderPlayer.getInventory().setItem(itemHolderPlayer.getInventory().getHeldItemSlot(), soulBoundItem);
        Util.sendMessage(itemHolderPlayer, config.bindSuccess);
        Util.bindEffect(itemHolderPlayer);
    }


    protected Optional<Player> findPlayerFromName(String name) {

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    protected boolean mainHandEmpty(Player player) {
        return player.getInventory().getItemInMainHand().getType().equals(Material.AIR);
    }
}
