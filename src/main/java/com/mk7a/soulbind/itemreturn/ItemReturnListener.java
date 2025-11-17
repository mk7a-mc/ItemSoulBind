package com.mk7a.soulbind.itemreturn;

import com.mk7a.soulbind.listeners.Access;
import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.main.PluginPermissions;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ItemReturnListener implements Listener {

    private final ItemSoulBindPlugin plugin;
    private final PluginConfiguration config;

    private final HashMap<String, ArrayList<ItemStack>> foundItems;
    private final HashMap<UUID, BukkitTask> playerTasks = new HashMap<>();


    protected ItemReturnListener(ItemSoulBindPlugin plugin, HashMap<String, ArrayList<ItemStack>> foundItems) {
        this.plugin = plugin;
        this.foundItems = foundItems;
        this.config = ItemSoulBindPlugin.getPluginConfig();
    }

    protected void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        startPlayerTask(event.getPlayer());
    }

    private void startPlayerTask(Player player) {

        UUID uuid = player.getUniqueId();
        BukkitTask task = new ItemReturnTask(plugin, foundItems, player).runTaskTimer(plugin, 1, 20 * 60);
        playerTasks.putIfAbsent(uuid, task);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();
        if (playerTasks.containsKey(uuid)) {
            playerTasks.get(uuid).cancel();
            playerTasks.remove(uuid);
        }
    }

    protected void refreshPlayers() {

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            startPlayerTask(player);
        }
    }

    private void handleItemReturn(Player sourcePlayer, ItemStack checkedItem, Access accessLevel) {

        String playerNotifyString;
        String adminNotifyString;

        if (accessLevel == Access.DENY_PLAYER) {

            String itemOwnerUUID = BindUtil.getPlayerOwner(checkedItem);
            foundItems.putIfAbsent(itemOwnerUUID, new ArrayList<>());
            ArrayList<ItemStack> foundItemsList = foundItems.get(itemOwnerUUID);
            foundItemsList.add(checkedItem);
            sourcePlayer.playSound(sourcePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1F, 1F);

            adminNotifyString = Util.color(String.format("&c(%s) %s", sourcePlayer.getName(), config.detectedItemBroadcast));
            playerNotifyString = config.detectedItemMessage;


        } else { // Access.DENY_GROUP

            adminNotifyString = Util.color(String.format("&c(%s) %s", sourcePlayer.getName(), config.detectedItemBroadcastGroup));
            playerNotifyString = config.detectedItemMessageGroup;
        }

        Util.sendMessage(sourcePlayer, playerNotifyString);

        if (config.consoleLogDetection) {
            plugin.getLogger().info(adminNotifyString);
        }

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasPermission(PluginPermissions.NOTIFY)) {
                onlinePlayer.sendMessage(adminNotifyString);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void invClickReturn(InventoryClickEvent event) {

        // Prevent return system triggering on items in GUIs where click is cancelled.
        // Event priority set to HIGHEST to ensure later execution.
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (Util.canIgnoreSoulBind(player)) {
            return;
        }

        var clickedOnItem = event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta();
        var clickedWithItem = event.getCursor() != null && event.getCursor().hasItemMeta();

        if ((clickedOnItem || clickedWithItem)
                && event.getClickedInventory() != null && event.getClickedInventory().getHolder() != null) {

            ItemStack checkedItem;
            if (clickedOnItem) {
                checkedItem  = event.getCurrentItem();
            } else { //clickedWithItem
                checkedItem = event.getCursor();
            }

            var clickInsideOwnInventory = event.getClickedInventory().getHolder().equals(player);
            Access accessLevel = BindUtil.getAccessLevel(checkedItem, player);

            if (accessLevel != Access.ALLOW && clickInsideOwnInventory) {

                if (clickedWithItem) {
                    // Remove and return item on next tick
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        var inventory = player.getInventory();
                        for (int i = 0; i < inventory.getSize(); i++) {
                            ItemStack item = inventory.getItem(i);
                            if (item != null && item.hasItemMeta()) {
                                var access = BindUtil.getAccessLevel(item, player);
                                if (access != Access.ALLOW) {
                                    inventory.setItem(i, new ItemStack(Material.AIR));
                                    handleItemReturn(player, item, access);
                                }
                            }
                        }
                    });

                } else { // ClickedOnItem
                    // Cancel event, immediately remove and return item
                    event.setCancelled(true);
                    player.getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
                    handleItemReturn(player, checkedItem, accessLevel);
                }
            }
        }
    }


    @EventHandler
    public void invClickGUI(InventoryCloseEvent event) {
        boolean isReturnGUI = event.getInventory().getHolder() instanceof ItemReturnGUIHolder
                && event.getInventory().getType() == InventoryType.DROPPER;
        if (isReturnGUI) {
            for (ItemStack item : event.getInventory().getContents()) {
                if (item != null) {
                    event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), item);
                }
            }
        }
    }
}
