package com.mk7a.soulbind.listeners;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.main.PluginPermissions;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ItemProtectionListener implements Listener {

    private final ItemSoulBindPlugin plugin;
    private final PluginConfiguration config;
    private final HashMap<UUID, Map<Integer, ItemStack>> deathItems = new HashMap<>();

    public ItemProtectionListener(ItemSoulBindPlugin plugin) {
        this.plugin = plugin;
        config = ItemSoulBindPlugin.getPluginConfig();
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {

        if (event.getWhoClicked() instanceof Player player) {

            if (Util.canIgnoreSoulBind(player)) {
                return;
            }

            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()
                    && event.getClickedInventory() != null && event.getClickedInventory().getHolder() != null
                    && !event.getClickedInventory().getHolder().equals(player)) {

                ItemStack clickedItem = event.getCurrentItem();
                Access access = BindUtil.getAccessLevel(clickedItem, player);
                if (access != Access.ALLOW) {

                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);

                    switch (access) {
                        case DENY_PLAYER -> Util.sendMessage(player, config.denyMsg);
                        case DENY_GROUP -> Util.sendMessage(player, config.denyMsgGroup);
                    }
                }
            }

        }
    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {

        if (event.getEntity() instanceof Player player) {

            if (Util.canIgnoreSoulBind(player)) {
                return;
            }

            ItemStack item = event.getItem().getItemStack();

            if (BindUtil.getAccessLevel(item, player) != Access.ALLOW) {

                event.setCancelled(true);
            }

        }
    }

    /**
     * Extract soul bound items early in death event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeathEarly(PlayerDeathEvent event) {

        if (!event.getKeepInventory() && event.getEntity().hasPermission(PluginPermissions.KEEP_ON_DEATH)) {

            Map<Integer, ItemStack> boundItemsInPosition = new HashMap<>();
            ItemStack[] inventory = event.getEntity().getInventory().getContents();

            for (int i=0; i < inventory.length; i++) {
                ItemStack item = inventory[i];

                if (item != null && BindUtil.hasBind(item)) {
                    boundItemsInPosition.put(i, item);
                }
            }

            if (!boundItemsInPosition.keySet().isEmpty()) {
                event.getDrops().removeAll(boundItemsInPosition.values());
                deathItems.put(event.getEntity().getUniqueId(), boundItemsInPosition);
            }

        }
    }

    /**
     * Final check to ensure other plugins have not enabled keep inventory. If they have,
     * cancel replacement of soul bound items by removing from deathItems.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeathLate(PlayerDeathEvent event) {
        var uuid = event.getEntity().getUniqueId();
        if (event.getKeepInventory()) {
            deathItems.remove(uuid);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {

        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (deathItems.containsKey(playerUUID)) {

            Map<Integer, ItemStack> items = deathItems.get(playerUUID);
            PlayerInventory playerInventory = player.getInventory();


            for (Integer i : items.keySet()) {

                if (playerInventory.getItem(i) == null) {

                    playerInventory.setItem(i, items.get(i));

                } else {
                    // In case another plugin has occupied original slot of item,
                    playerInventory.addItem(items.get(i));
                }

            }

            deathItems.remove(playerUUID);
        }

    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {

        if (!config.preventPlacing) {
            return;
        }

        ItemStack handItem = event.getItemInHand();

        if (!handItem.getType().isBlock()) {
            return;
        }

        if (BindUtil.hasBind(handItem)) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {

        Player player = event.getPlayer();

        if (Util.canIgnoreSoulBind(player)) {
            return;
        }

        ItemStack item = event.getArmorStandItem();

        if (item.hasItemMeta() && BindUtil.getAccessLevel(item, player) != Access.ALLOW) {

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorDispense(BlockDispenseArmorEvent event) {

        if (!(event.getTargetEntity() instanceof Player player)) {
            return;
        }

        if (Util.canIgnoreSoulBind(player)) {
            return;
        }
        ItemStack item = event.getItem();
        if (BindUtil.hasBind(item) && BindUtil.getAccessLevel(item, player) != Access.ALLOW) {
            event.setCancelled(true);
        }
    }

}
