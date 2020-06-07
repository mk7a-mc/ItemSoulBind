package com.mk7a.soulbind.listeners;

import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.main.PluginPermissions;
import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class ItemProtectionListener implements Listener {

    private final ItemSoulBindPlugin plugin;
    private final PluginConfiguration config;
    private final HashMap<UUID, ItemStack[]> deathItems = new HashMap<>();

    public ItemProtectionListener(ItemSoulBindPlugin plugin) {
        this.plugin = plugin;
        config = ItemSoulBindPlugin.getPluginConfig();
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {

        if (event.getWhoClicked() instanceof Player) {

            Player player = (Player) event.getWhoClicked();

            if (player.hasPermission(PluginPermissions.BYPASS) || player.getGameMode().equals(GameMode.CREATIVE)) {
                return;
            }

            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()
                    && event.getClickedInventory() != null && event.getClickedInventory().getHolder() != null
                    && !event.getClickedInventory().getHolder().equals(player)) {

                ItemStack clickedItem = event.getCurrentItem();

                if (!BindUtil.hasAccess(clickedItem, player)) {

                    event.setCancelled(true);
                    Util.sendMessage(player, config.denyMsg);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);

                }
            }

        }
    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {

        if (event.getEntity() instanceof Player ) {

            Player player = (Player) event.getEntity();

            if (player.hasPermission(PluginPermissions.BYPASS) || player.getGameMode().equals(GameMode.CREATIVE)) {
                return;
            }

            ItemStack item = event.getItem().getItemStack();

            if (!BindUtil.hasAccess(item, player)) {

                event.setCancelled(true);
            }

        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(PlayerDeathEvent event) {

        if (!event.getKeepInventory() && event.getEntity().hasPermission(PluginPermissions.KEEP_ON_DEATH)) {

            List<ItemStack> drops = event.getDrops();
            List<ItemStack> boundItemsInDrops = new ArrayList<>();

            if (drops.size() > 0) {
                for (ItemStack i : drops) {
                    if (i != null && BindUtil.hasOwner(i)) {
                        boundItemsInDrops.add(i);
                    }
                }
            }

            ItemStack[] inventory = event.getEntity().getInventory().getContents();

            for (int i=0; i < inventory.length; i++) {
                ItemStack item = inventory[i];

                // Remove non bound items
                if (item != null && !BindUtil.hasOwner(item)) {
                    inventory[i] = null;
                }
            }

            if (boundItemsInDrops.size() > 0) {
                event.getDrops().removeAll(boundItemsInDrops);
                deathItems.put(event.getEntity().getUniqueId(), inventory);
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {

        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (deathItems.containsKey(playerUUID)) {

            player.getInventory().setContents(deathItems.get(playerUUID));
            deathItems.remove(playerUUID);
        }

    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {

        if (config.preventPlacing) {
            ItemStack placedItem = event.getItemInHand();
            if (BindUtil.hasOwner(placedItem)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {

        Player player = event.getPlayer();

        if (player.hasPermission(PluginPermissions.BYPASS) || player.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        ItemStack item = event.getArmorStandItem();

        if (item.hasItemMeta() && !BindUtil.hasAccess(item, player)) {

            event.setCancelled(true);
        }
    }

}
