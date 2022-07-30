package com.mk7a.soulbind.listeners;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.main.PluginPermissions;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class ItemModificationListener implements Listener {

    private final ItemSoulBindPlugin plugin;
    private PluginConfiguration config;

    public ItemModificationListener(ItemSoulBindPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        config = ItemSoulBindPlugin.getPluginConfig();
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {

        Player player = (Player) event.getWhoClicked();

        if (!config.preventCraft || player.hasPermission(PluginPermissions.BYPASS_CRAFT)) {
            return;
        }

        ItemStack[] matrix = event.getInventory().getMatrix();

        for (ItemStack item : matrix) {
            if (item != null) {
                if (BindUtil.hasBind(item)) {

                    Util.sendMessage(player, config.craftDeny);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {

        Player player = (Player) event.getInventory().getViewers().get(0);

        if (!config.preventEnchant || player.hasPermission(PluginPermissions.BYPASS_ENCHANT)) {
            return;
        }

        if (BindUtil.hasBind(event.getItem())) {

            Util.sendMessage(player, config.enchantDeny);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAnvilClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();

        if (!config.preventAnvil || player.hasPermission(PluginPermissions.BYPASS_ANVIL)) {
            return;
        }

        boolean isAnvilOutputSlotClick = event.getClickedInventory() != null
                && event.getClickedInventory().getType().equals(InventoryType.ANVIL)
                && event.getSlot() == 2;

        if (isAnvilOutputSlotClick) {

            ItemStack[] anvil = event.getClickedInventory().getContents();

            for (ItemStack item : anvil) {
                if (item != null) {
                    if (BindUtil.hasBind(item)) {

                        Util.sendMessage(player, config.anvilDeny);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }


}
