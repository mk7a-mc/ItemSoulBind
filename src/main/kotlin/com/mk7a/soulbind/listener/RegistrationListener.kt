package com.mk7a.soulbind.listener

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import com.mk7a.soulbind.bind.SoulBinder
import com.mk7a.soulbind.bind.SpecialBind
import com.mk7a.soulbind.bind.ignoresSoulBind
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Tag
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

/** Binds items carrying a register string or special bind line in their lore. */
class RegistrationListener(private val binder: SoulBinder) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        if (inventory.type == InventoryType.MERCHANT) return
        val player = event.whoClicked as? Player ?: return
        if (player.ignoresSoulBind) return
        val clickedItem = event.currentItem ?: return

        if (event.click == ClickType.NUMBER_KEY) {
            val hotbarItem = player.inventory.getItem(event.hotbarButton)
            if (hotbarItem != null && tryBind(player, hotbarItem)) {
                player.inventory.setItem(event.hotbarButton, hotbarItem)
                event.isCancelled = true
            }
        }

        if (tryBind(player, clickedItem)) {
            event.currentItem = clickedItem
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEquip(event: PlayerInteractEvent) {
        if (!event.action.isRightClick) return
        val item = event.item ?: return
        if (ARMOR.none { it.isTagged(item.type) }) return

        bindOnTrigger(event.player, item, SpecialBind.ON_EQUIP)
    }

    /** A player is only the direct damager for melee hits; projectiles are their own damager. */
    @EventHandler
    fun onMeleeHit(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        val weapon = player.inventory.itemInMainHand
        if (WEAPON_COMPONENTS.none(weapon::hasData)) return

        bindOnTrigger(player, weapon, SpecialBind.ON_USE)
    }

    /** Bows and crossbows, from either hand. */
    @EventHandler
    fun onBowShot(event: EntityShootBowEvent) {
        val player = event.entity as? Player ?: return
        bindOnTrigger(player, event.bow ?: return, SpecialBind.ON_USE)
    }

    /**
     * Thrown weapons, such as tridents. The projectile already carries its own copy of the item, to
     * be picked up again later, so that copy is the one to bind.
     */
    @EventHandler(ignoreCancelled = true)
    fun onWeaponThrow(event: PlayerLaunchProjectileEvent) {
        val thrown = event.projectile as? AbstractArrow ?: return
        if (WEAPON_COMPONENTS.none(event.itemStack::hasData)) return

        val stack = thrown.itemStack
        if (bindOnTrigger(event.player, stack, SpecialBind.ON_USE)) thrown.itemStack = stack
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        bindOnTrigger(event.player, event.player.inventory.itemInMainHand, SpecialBind.ON_USE)
    }

    @EventHandler
    fun onPickup(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        if (player.ignoresSoulBind) return

        val stack = event.item.itemStack
        if (tryBind(player, stack, SpecialBind.ON_PICKUP)) {
            event.item.itemStack = stack
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val player = event.player
        if (player.ignoresSoulBind) return

        val stack = event.itemDrop.itemStack
        if (tryBind(player, stack)) {
            event.itemDrop.itemStack = stack
            event.isCancelled = true
        }
    }

    private fun bindOnTrigger(player: Player, item: ItemStack, special: SpecialBind): Boolean =
        !player.ignoresSoulBind && tryBind(player, item, special)

    private fun tryBind(player: Player, item: ItemStack, vararg special: SpecialBind): Boolean =
        binder.bindFromLore(item, player, *special).also { bound -> if (bound) binder.playBindEffect(player) }

    private companion object {
        // Data driven, so new and custom weapons count without a list of materials
        val WEAPON_COMPONENTS = listOf(
            DataComponentTypes.WEAPON,
            DataComponentTypes.PIERCING_WEAPON,
            DataComponentTypes.KINETIC_WEAPON,
        )
        val ARMOR = listOf(Tag.ITEMS_HEAD_ARMOR, Tag.ITEMS_CHEST_ARMOR, Tag.ITEMS_LEG_ARMOR, Tag.ITEMS_FOOT_ARMOR)
    }
}
