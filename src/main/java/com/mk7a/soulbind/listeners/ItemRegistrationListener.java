package com.mk7a.soulbind.listeners;


import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ItemRegistrationListener implements Listener {

    private static final String USERNAME_PLACEHOLDER = "%username%";
    private static final String GROUP_PLACEHOLDER = "%group%";
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

        ItemStack clickedItem = event.getCurrentItem();

        Optional<ItemStack> groupBindItem = bindIfContainsGroupString(player, clickedItem);
        if (groupBindItem.isPresent()) {
            event.setCurrentItem(groupBindItem.get());
            event.setCancelled(true);
            return;
        }
        Optional<ItemStack> regItem = bindIfContainsString(player, clickedItem, config.registerString);
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

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (Util.canIgnoreSoulBind(player)) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (mainHand.getType().toString().contains(SWORD)) {

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

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (Util.canIgnoreSoulBind(player)) {
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

        if (Util.canIgnoreSoulBind(player)) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();

        bindIfContainsString(player, mainHand, config.bindOnUseString);
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


        Optional<ItemStack> groupBindItem = bindIfContainsGroupString(player, pickupItem);
        if (groupBindItem.isPresent()) {
            event.getItem().setItemStack(groupBindItem.get());
            event.setCancelled(true);
            return;
        }

        Optional<ItemStack> nonGroupPickupItem =
                bindIfContainsString(player, pickupItem, config.registerString, config.bindOnPickupString);
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

        Optional<ItemStack> groupBindItem = bindIfContainsGroupString(player, droppedItem);
        if (groupBindItem.isPresent()) {
            event.getItemDrop().setItemStack(groupBindItem.get());
            event.setCancelled(true);
            return;
        }


        Optional<ItemStack> nonGroupPickupItem = bindIfContainsString(player, droppedItem, config.registerString);
        if (nonGroupPickupItem.isPresent()) {
            event.getItemDrop().setItemStack(nonGroupPickupItem.get());
            event.setCancelled(true);
        }

    }


    private Optional<ItemStack> bindIfContainsString(Player player, ItemStack item, String... requiredString) {

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int loreLine = 0; loreLine < lore.size(); loreLine++) {

                String lineText = ChatColor.stripColor(lore.get(loreLine));

                for (String reqStrOption : requiredString) {
                    reqStrOption = ChatColor.stripColor(reqStrOption);

                    if (lineText.contains(reqStrOption)) {
                        ItemStack boundItem = applyLoreTriggeredBind(player, item, loreLine);
                        item.setItemMeta(boundItem.getItemMeta());
                        Util.bindEffect(player);

                        return Optional.of(boundItem);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<ItemStack> bindIfContainsGroupString(Player player, ItemStack item) {

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 0; i < lore.size(); i++) {

                String line = lore.get(i);
                String lineText = ChatColor.stripColor(line);

                String groupRegString = ChatColor.stripColor(config.groupRegisterString);
                if (lineText.contains(groupRegString)) {

                    String permissionSuffix = lineText.split(groupRegString)[1].split(" ")[0];
                    ItemStack regItem = applyGroupBind(permissionSuffix, item, i);

                    item.setItemMeta(regItem.getItemMeta());
                    Util.bindEffect(player);

                    return Optional.of(regItem);


                }
            }
        }
        return Optional.empty();
    }


    private ItemStack applyLoreTriggeredBind(Player player, ItemStack item, int lineIndex) {

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        if (config.displayLore) {
            lore.set(lineIndex, config.loreMsg.replaceAll(USERNAME_PLACEHOLDER, player.getName()));
        } else {
            lore.remove(lineIndex);
        }
        meta.setLore(lore);

        item.setItemMeta(meta);

        return BindUtil.setPlayerOwner(item, player);
    }

    private ItemStack applyGroupBind(String permission, ItemStack item, int lineIndex) {

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        if (config.displayLore) {
            lore.set(lineIndex, config.loreMsgGroup.replaceAll(GROUP_PLACEHOLDER, permission));
        } else {
            lore.remove(lineIndex);
        }
        meta.setLore(lore);

        item.setItemMeta(meta);

        return BindUtil.setGroupOwner(item, permission);
    }


}