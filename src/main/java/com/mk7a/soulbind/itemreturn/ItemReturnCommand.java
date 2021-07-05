package com.mk7a.soulbind.itemreturn;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginConfiguration;
import com.mk7a.soulbind.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemReturnCommand implements CommandExecutor {

    protected static final String RETURN_ITEMS = "returnItems";
    private static final InventoryType RETURN_INV_TYPE = InventoryType.DROPPER;

    private final ItemSoulBindPlugin plugin;
    private final PluginConfiguration pluginConfig;

    private final HashMap<String, ArrayList<ItemStack>> foundItems;

    protected ItemReturnCommand(ItemSoulBindPlugin plugin, HashMap<String, ArrayList<ItemStack>> foundItems) {
        this.plugin = plugin;
        this.foundItems = foundItems;
        this.pluginConfig = ItemSoulBindPlugin.getPluginConfig();
    }

    protected void register() {
        plugin.getCommand(ItemReturnCommand.RETURN_ITEMS).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player player)) {
            return false;
        }

        String uuid = player.getUniqueId().toString();

        if (!foundItems.containsKey(uuid)) {
            Util.sendMessage(player, pluginConfig.noItems);
            return true;
        }

        // Use custom InventoryHolder to identify GUI in inventory click event
        Inventory getItemsGUI = plugin.getServer().createInventory(new ItemReturnGUIHolder(), RETURN_INV_TYPE, ItemReturnModule.INV_TITLE);

        ArrayList<ItemStack> foundItemsList = foundItems.get(uuid);

        new ArrayList<>(foundItemsList).stream().limit(RETURN_INV_TYPE.getDefaultSize()).forEach(item -> {
            foundItemsList.remove(item);
            getItemsGUI.addItem(item);
        });

        if (foundItemsList.isEmpty()) {
            foundItems.remove(uuid);
        }

        player.openInventory(getItemsGUI);

        return true;


    }
}
