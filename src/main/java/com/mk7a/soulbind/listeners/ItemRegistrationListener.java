package com.mk7a.soulbind.listeners;


import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.util.BindStringUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class ItemRegistrationListener implements Listener {


    private static final String[] armors = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};
    private static final String SWORD = "SWORD";

    private final ItemSoulBindPlugin plugin;
    private final PluginConfiguration config;

    public ItemRegistrationListener(ItemSoulBindPlugin plugin) {
        this.plugin = plugin;
        config = ItemSoulBindPlugin.getPluginConfig();
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    /**
     * Normal binding when string found in lore. Listens for inventory click.
     */
    @EventHandler
    public void onInvClick(InventoryClickEvent event) {

        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.getClickedInventory().getType().equals(InventoryType.MERCHANT)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (Util.canIgnoreSoulBind(player)) {
            return;
        }

        if (event.getCurrentItem() == null) {
            return;
        }

        if (event.getClick().equals(ClickType.NUMBER_KEY)) {

            var hotbarSlot = event.getHotbarButton();
            var hotbarItem = player.getInventory().getItem(hotbarSlot);

            if (Objects.nonNull(hotbarItem)) {

                Optional<ItemStack> groupBindItem = BindStringUtil.bindIfContainsGroupString(player, hotbarItem);
                if (groupBindItem.isPresent()) {
                    player.getInventory().setItem(hotbarSlot, groupBindItem.get());
                    event.setCancelled(true);
                } else {
                    Optional<ItemStack> regItem = BindStringUtil.bindIfContainsString(player, hotbarItem, config.registerString);
                    if (regItem.isPresent()) {
                        player.getInventory().setItem(hotbarSlot, regItem.get());
                        event.setCancelled(true);
                    }
                }
            }
        }

        ItemStack clickedItem = event.getCurrentItem();

        Optional<ItemStack> groupBindItem = BindStringUtil.bindIfContainsGroupString(player, clickedItem);
        if (groupBindItem.isPresent()) {
            event.setCurrentItem(groupBindItem.get());
            event.setCancelled(true);
            return;
        }
        Optional<ItemStack> regItem = BindStringUtil.bindIfContainsString(player, clickedItem, config.registerString);
        if (regItem.isPresent()) {
            event.setCurrentItem(regItem.get());
            event.setCancelled(true);
        }
    }


    /**
     * Bind on equip armor listener.
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if (!config.bindOnEquip) {
            return;
        }

        Player player = event.getPlayer();

        if (Util.canIgnoreSoulBind(player)) {
            return;
        }

        Action action = event.getAction();

        if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {

            if (event.getItem() == null) {
                return;
            }

            ItemStack eventItem = event.getItem();

            if (isArmor(eventItem)) {

                BindStringUtil.bindIfContainsString(player, eventItem, config.bindOnEquipString);
            }
        }

    }

    private boolean isArmor(ItemStack item) {

        return Arrays.stream(armors).anyMatch(a -> item.getType().toString().endsWith(a));
    }


    /**
     * Bind on sword use listener
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        if (!config.bindOnUse) {
            return;
        }

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (Util.canIgnoreSoulBind(player)) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (mainHand.getType().toString().contains(SWORD)) {

            BindStringUtil.bindIfContainsString(player, mainHand, config.bindOnUseString);
        }

    }


    /**
     * Bind on bow use listener
     */
    @EventHandler
    public void onBow(EntityShootBowEvent event) {

        if (!config.bindOnUse) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (Util.canIgnoreSoulBind(player)) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();

        BindStringUtil.bindIfContainsString(player, mainHand, config.bindOnUseString);

    }


    /**
     * Bind on tool use to break block
     */
    @EventHandler
    public void onBreak(BlockBreakEvent event) {

        if (!config.bindOnUse) {
            return;
        }

        Player player = event.getPlayer();

        if (Util.canIgnoreSoulBind(player)) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();

        BindStringUtil.bindIfContainsString(player, mainHand, config.bindOnUseString);
    }


    /**
     * Listener for normal pickup and bind on pickup
     */
    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (Util.canIgnoreSoulBind(player)) {
            return;
        }

        ItemStack pickupItem = event.getItem().getItemStack();


        Optional<ItemStack> groupBindItem = BindStringUtil.bindIfContainsGroupString(player, pickupItem);
        if (groupBindItem.isPresent()) {
            event.getItem().setItemStack(groupBindItem.get());
            event.setCancelled(true);
            return;
        }

        Optional<ItemStack> nonGroupPickupItem = BindStringUtil.bindIfContainsString(
                player, pickupItem, config.registerString, config.bindOnPickupString);
        if (nonGroupPickupItem.isPresent()) {
            event.getItem().setItemStack(nonGroupPickupItem.get());
            event.setCancelled(true);
        }

    }

    /**
     * Listener for binding on drop
     */
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {

        Player player = event.getPlayer();

        if (Util.canIgnoreSoulBind(player)) {
            return;
        }

        ItemStack droppedItem = event.getItemDrop().getItemStack();

        Optional<ItemStack> groupBindItem = BindStringUtil.bindIfContainsGroupString(player, droppedItem);
        if (groupBindItem.isPresent()) {
            event.getItemDrop().setItemStack(groupBindItem.get());
            event.setCancelled(true);
            return;
        }


        Optional<ItemStack> nonGroupPickupItem = BindStringUtil.bindIfContainsString(player, droppedItem, config.registerString);
        if (nonGroupPickupItem.isPresent()) {
            event.getItemDrop().setItemStack(nonGroupPickupItem.get());
            event.setCancelled(true);
        }

    }

}