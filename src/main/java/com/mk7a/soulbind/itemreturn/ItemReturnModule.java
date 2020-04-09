package com.mk7a.soulbind.itemreturn;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemReturnModule {

    static final String INV_TITLE = "Returned Soul Bound Items";

    private final ItemSoulBindPlugin plugin;
    private final HashMap<String, ArrayList<ItemStack>> foundItems = new HashMap<>();



    public ItemReturnModule(ItemSoulBindPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {

        ItemReturnListener listener = new ItemReturnListener(plugin, foundItems);
        listener.register();
        listener.refreshPlayers();

        new ItemReturnCommand(plugin, foundItems).register();

    }

}
