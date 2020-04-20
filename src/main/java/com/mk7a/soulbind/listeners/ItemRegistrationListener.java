package com.mk7a.soulbind.listeners;


import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.main.PluginPermissions;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemRegistrationListener implements Listener {

    private static final String USERNAME = "%username%";
    private static final String[] armors = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};
    private static final Material[] swords = {Material.STONE_SWORD,
            Material.DIAMOND_SWORD,
            Material.GOLDEN_SWORD,
            Material.IRON_SWORD,
            Material.WOODEN_SWORD};

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

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (canIgnoreSoulBind(player)) {
            return;
        }

        if (event.getCurrentItem() == null) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();

        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasLore()) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        List<String> lore = meta.getLore();

        for (String line : lore) {

            String lineText = ChatColor.stripColor(line);

            if (lineText.contains(config.registerString)) {

                ItemStack regItem = doBind(player, clickedItem, line);

                event.setCurrentItem(regItem);
                event.setCancelled(true);

                return;
            }
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

        if(canIgnoreSoulBind(player)) {
            return;
        }

        Action action = event.getAction();

        if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {

            if (event.getItem() == null) {
                return;
            }

            ItemStack eventItem = event.getItem();

            if (isArmor(eventItem)) {

                bindIfContainsString(player, eventItem, config.bindOnEquipString);
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

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();

        if (canIgnoreSoulBind(player)) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (Arrays.asList(swords).contains(mainHand.getType())) {

            bindIfContainsString(player, mainHand, config.bindOnUseString);
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

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (canIgnoreSoulBind(player)) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();

        bindIfContainsString(player, mainHand, config.bindOnUseString);

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

        if (canIgnoreSoulBind(player)) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();

        bindIfContainsString(player, mainHand, config.bindOnUseString);
    }


    /**
     * Listener for normal pick up and bind on pickup
     */
    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (canIgnoreSoulBind(player)) {
            return;
        }

        ItemStack pickupItem = event.getItem().getItemStack();

        if (!pickupItem.hasItemMeta()) {
            return;
        }

        ItemMeta meta = pickupItem.getItemMeta();

        if (!meta.hasLore() || meta.getLore().size() == 0) {
            return;
        }

        List<String> lore = meta.getLore();

        for (String line : lore) {

            String lineText = ChatColor.stripColor(line);

            boolean normalPickup = lineText.contains(config.registerString);
            boolean bindOnPickup = config.bindOnPickup && lineText.contains(ChatColor.stripColor(config.bindOnPickupString));

            if (normalPickup || bindOnPickup) {

                ItemStack regItem = doBind(player, pickupItem, line);

                Util.bindEffect(player);

                event.getItem().setItemStack(regItem);
                event.setCancelled(true);

                return;
            }
        }

    }

    /**
     * Listener for binding on drop
     */
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {

        Player player = event.getPlayer();

        if (player.hasPermission(PluginPermissions.BYPASS) ||
                player.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        ItemStack puItem = event.getItemDrop().getItemStack();

        if (!puItem.hasItemMeta()) {
            return;
        }

        ItemMeta meta = puItem.getItemMeta();

        if (meta.hasLore()) {

            List<String> lore = meta.getLore();

            for (String line : lore) {

                if (line.contains(config.registerString)) {

                    ItemStack regItem = doBind(player, puItem, line);

                    event.getItemDrop().setItemStack(regItem);
                    event.setCancelled(true);

                    return;
                }
            }
        }

    }


    private void bindIfContainsString(Player player, ItemStack item, String requiredString) {

        String trigger = ChatColor.stripColor(requiredString);

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (String line : lore) {

                String lineText = ChatColor.stripColor(line);

                if (lineText.contains(trigger)) {

                    ItemStack boundItem = doBind(player, item, line);
                    item.setItemMeta(boundItem.getItemMeta());
                    Util.bindEffect(player);

                    return;
                }
            }
        }
    }


    private ItemStack doBind(Player player, ItemStack item, String line) {

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        int index = lore.indexOf(line);
        if (config.displayLore) {
            lore.set(index, config.loreMsg.replaceAll(USERNAME, player.getName()));
        } else {
            lore.remove(line);
        }
        meta.setLore(lore);

        item.setItemMeta(meta);

        return BindUtil.setOwner(item, player);
    }



    private boolean canIgnoreSoulBind(Player player) {

        return player.hasPermission(PluginPermissions.BYPASS) ||  player.getGameMode().equals(GameMode.CREATIVE);
    }


}