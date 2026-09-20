package com.mk7a.soulbind.listener

import com.mk7a.soulbind.ItemSoulBindPlugin
import com.mk7a.soulbind.Permissions
import com.mk7a.soulbind.bind.isSoulBound
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.SmithingInventory

/** Optional restrictions on crafting with, enchanting, repairing and smithing soul bound items. */
class ModificationListener(private val plugin: ItemSoulBindPlugin) : Listener {

    private val settings get() = plugin.settings

    @EventHandler
    fun onCraft(event: CraftItemEvent) {
        val player = event.whoClicked as? Player ?: return
        if (!settings.preventCraft || player.hasPermission(Permissions.BYPASS_CRAFT)) return

        if (event.inventory.matrix.any { it != null && it.isSoulBound }) {
            event.deny(player, settings.messages.craftDeny)
        }
    }

    @EventHandler
    fun onEnchant(event: EnchantItemEvent) {
        val player = event.enchanter
        if (!settings.preventEnchant || player.hasPermission(Permissions.BYPASS_ENCHANT)) return

        if (event.item.isSoulBound) event.deny(player, settings.messages.enchantDeny)
    }

    @EventHandler
    fun onAnvilResultClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (!settings.preventAnvil || player.hasPermission(Permissions.BYPASS_ANVIL)) return

        val anvil = event.clickedInventory as? AnvilInventory ?: return
        if (event.slotType != InventoryType.SlotType.RESULT) return

        // Inputs only: the result of a soul bind book combine is itself bound
        if (listOfNotNull(anvil.firstItem, anvil.secondItem).any { it.isSoulBound }) {
            event.deny(player, settings.messages.anvilDeny)
        }
    }

    @EventHandler
    fun onSmithingResultClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (!settings.preventSmithing || player.hasPermission(Permissions.BYPASS_SMITHING)) return

        val smithing = event.clickedInventory as? SmithingInventory ?: return
        if (event.slotType != InventoryType.SlotType.RESULT) return

        val inputs = listOfNotNull(smithing.inputTemplate, smithing.inputEquipment, smithing.inputMineral)
        if (inputs.any { it.isSoulBound }) event.deny(player, settings.messages.smithingDeny)
    }

    private fun Cancellable.deny(player: Player, message: Component) {
        isCancelled = true
        player.sendMessage(message)
        player.playDenySound()
    }
}
