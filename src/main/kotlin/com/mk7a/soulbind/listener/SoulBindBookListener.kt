package com.mk7a.soulbind.listener

import com.mk7a.soulbind.ItemSoulBindPlugin
import com.mk7a.soulbind.Permissions
import com.mk7a.soulbind.bind.SoulBinder
import com.mk7a.soulbind.bind.isSoulBound
import com.mk7a.soulbind.enchant.SoulboundEnchantment
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.inventory.PrepareGrindstoneEvent

/** Turns a soul bind book combined onto an item into a bind to the player using the anvil. */
class SoulBindBookListener(private val plugin: ItemSoulBindPlugin, private val binder: SoulBinder) : Listener {

    /**
     * Vanilla has already merged the book's enchantment into the result by this point. Binding the
     * result here means the anvil previews the bound item, and whatever way it is taken out, it is
     * already bound.
     */
    @EventHandler
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        val result = event.result ?: return
        if (result.type == Material.ENCHANTED_BOOK || !SoulboundEnchantment.isOn(result) || result.isSoulBound) return

        val player = event.view.player as? Player ?: return
        if (!player.hasPermission(Permissions.BOOK_USE)) {
            event.result = null
            return
        }
        binder.bindToPlayer(result, player)
        event.result = result
    }

    /**
     * A grindstone strips the enchantment, which would leave an item that is bound but doesn't show
     * it. Depending on config the bind either goes with it, or the enchantment is put back.
     */
    @EventHandler
    fun onPrepareGrindstone(event: PrepareGrindstoneEvent) {
        val result = event.result ?: return
        if (!result.isSoulBound) return

        val inputs = listOfNotNull(event.inventory.upperItem, event.inventory.lowerItem)
        if (inputs.none(SoulboundEnchantment::isOn)) return

        if (plugin.settings.grindstoneUnbinds) binder.unbind(result) else SoulboundEnchantment.addTo(result)
        event.result = result
    }
}
