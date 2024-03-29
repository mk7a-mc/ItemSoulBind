package com.mk7a.soulbind.itemreturn;

import com.mk7a.soulbind.listeners.Access;
import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.main.PluginPermissions;
import com.mk7a.soulbind.util.BindUtil;
import com.mk7a.soulbind.util.Util;
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

        if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()
                && event.getClickedInventory() != null && event.getClickedInventory().getHolder() != null) {

            ItemStack clickedItem = event.getCurrentItem();
            boolean clickInsideOwnInventory = event.getClickedInventory().getHolder().equals(player);
            Access accessLevel = BindUtil.getAccessLevel(clickedItem, player);

            if (accessLevel != Access.ALLOW && clickInsideOwnInventory) {

                event.setCancelled(true);

                String playerNotifyString;
                String adminNotifyString;


                // If player bound, return item to player
                if (accessLevel == Access.DENY_PLAYER) {

                    player.getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
                    String itemOwnerUUID = BindUtil.getPlayerOwner(clickedItem);
                    foundItems.putIfAbsent(itemOwnerUUID, new ArrayList<>());
                    ArrayList<ItemStack> foundItemsList = foundItems.get(itemOwnerUUID);
                    foundItemsList.add(clickedItem);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1F, 1F);

                    adminNotifyString = Util.color(String.format("&c(%s) %s", player.getName(), config.detectedItemBroadcast));
                    playerNotifyString = config.detectedItemMessage;


                } else {

                    adminNotifyString = Util.color(String.format("&c(%s) %s", player.getName(), config.detectedItemBroadcastGroup));
                    playerNotifyString = config.detectedItemMessageGroup;
                }

                Util.sendMessage(player, playerNotifyString);

                if (config.consoleLogDetection) {
                    plugin.getLogger().info(adminNotifyString);
                }

                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission(PluginPermissions.NOTIFY)) {
                        onlinePlayer.sendMessage(adminNotifyString);
                    }
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
