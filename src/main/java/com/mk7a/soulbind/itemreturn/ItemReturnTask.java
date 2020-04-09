package com.mk7a.soulbind.itemreturn;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemReturnTask extends BukkitRunnable {

    private final ItemSoulBindPlugin plugin;
    private final HashMap<String, ArrayList<ItemStack>> foundItems;
    private final Player player;
    private final String uuid;

    protected ItemReturnTask(ItemSoulBindPlugin plugin, HashMap<String, ArrayList<ItemStack>> foundItems, Player player) {
        this.plugin = plugin;
        this.foundItems = foundItems;
        this.player = player;
        this.uuid = player.getUniqueId().toString();
    }

    @Override
    public void run() {

        if (foundItems.containsKey(uuid)) {

            Util.sendMessage(player, ItemSoulBindPlugin.getPluginConfig().foundItems);
        }
    }
}
