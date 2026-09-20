package com.mk7a.soulbind.itemreturn

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

/** Identifies the returned items GUI in inventory events. */
internal class ReturnedItemsHolder : InventoryHolder {

    private val inventory = Bukkit.createInventory(this, InventoryType.DROPPER, TITLE)

    override fun getInventory(): Inventory = inventory

    private companion object {
        val TITLE = Component.text("Returned Soul Bound Items")
    }
}
