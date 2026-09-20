package com.mk7a.soulbind.listener

import com.mk7a.soulbind.ItemSoulBindPlugin
import com.mk7a.soulbind.Permissions
import com.mk7a.soulbind.bind.Access
import com.mk7a.soulbind.bind.accessTo
import com.mk7a.soulbind.bind.ignoresSoulBind
import com.mk7a.soulbind.bind.isSoulBound
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.block.Shelf
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDispenseArmorEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.PlayerInventory
import org.bukkit.block.data.type.Shelf as ShelfData

/** Keeps soul bound items away from players without access to them. */
class ProtectionListener(private val plugin: ItemSoulBindPlugin) : Listener {

    private val settings get() = plugin.settings

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (player.ignoresSoulBind) return
        val item = event.currentItem ?: return
        // Items already in the player's own inventory are left to the item return system
        if ((event.clickedInventory as? PlayerInventory)?.holder == player) return

        val denyMessage = when (player.accessTo(item)) {
            Access.ALLOW -> return
            Access.DENY_PLAYER -> settings.messages.denyPlayer
            Access.DENY_GROUP -> settings.messages.denyGroup
        }
        event.isCancelled = true
        player.playDenySound()
        if (settings.showContainerDeny) player.sendMessage(denyMessage)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPickup(event: EntityPickupItemEvent) {
        val item = event.item.itemStack
        val player = event.entity as? Player
        if (player == null) {
            // Allays, foxes, piglins, zombies... an allay would go on to hand the item to another player
            if (item.isSoulBound) event.isCancelled = true
            return
        }
        if (player.ignoresSoulBind) return

        if (player.accessTo(item) != Access.ALLOW) event.isCancelled = true
    }

    /**
     * Arrows and tridents stuck in the world are collected through this event rather than the one
     * above. Vanilla only lets a trident's thrower take it back, but treats it as ownerless, and
     * free for anyone, while the thrower is offline.
     */
    @EventHandler(priority = EventPriority.LOW)
    fun onArrowPickup(event: PlayerPickupArrowEvent) {
        val player = event.player
        if (player.ignoresSoulBind) return

        if (player.accessTo(event.item.itemStack) != Access.ALLOW) event.isCancelled = true
    }

    /** Runs early so other plugins handling the drops never see the kept items. */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onDeath(event: PlayerDeathEvent) {
        if (event.keepInventory || !event.player.hasPermission(Permissions.KEEP_ON_DEATH)) return

        val bound = event.drops.filter { it.isSoulBound }
        event.drops -= bound
        event.itemsToKeep += bound
    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        if (!settings.preventPlacing) return

        val item = event.itemInHand
        if (item.type.isBlock && item.isSoulBound) event.isCancelled = true
    }

    @EventHandler
    fun onArmorStandManipulate(event: PlayerArmorStandManipulateEvent) {
        val player = event.player
        if (player.ignoresSoulBind) return

        if (player.accessTo(event.armorStandItem) != Access.ALLOW) event.isCancelled = true
    }

    @EventHandler
    fun onArmorDispense(event: BlockDispenseArmorEvent) {
        val player = event.targetEntity as? Player ?: return
        if (player.ignoresSoulBind) return

        if (player.accessTo(event.item) != Access.ALLOW) event.isCancelled = true
    }

    @EventHandler
    fun onShelfInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK || event.hand != EquipmentSlot.HAND) return
        val block = event.clickedBlock ?: return
        val player = event.player
        if (player.ignoresSoulBind) return

        val denied = when (val state = block.getState(false)) {
            is ChiseledBookshelf -> state.inventory.hasItemDeniedTo(player)
            is Shelf -> state.inventory.hasItemDeniedTo(player) || connectedShelves(block).any { it.inventory.hasItemDeniedTo(player) }
            else -> false
        }
        if (denied) {
            event.isCancelled = true
            player.playDenySound()
        }
    }

    /** Powered shelves swap their contents together with up to two aligned, powered neighbours per side. */
    private fun connectedShelves(origin: Block): Sequence<Shelf> {
        val data = origin.blockData as? ShelfData ?: return emptySequence()
        if (!data.isPowered) return emptySequence()

        return perpendicularFaces(data.facing).asSequence().flatMap { side ->
            generateSequence(origin.getRelative(side)) { it.getRelative(side) }
                .take(2)
                .takeWhile { neighbour ->
                    val neighbourData = neighbour.blockData as? ShelfData
                    neighbourData != null && neighbourData.isPowered && neighbourData.facing == data.facing
                }
                .mapNotNull { it.getState(false) as? Shelf }
        }
    }

    private fun perpendicularFaces(facing: BlockFace): List<BlockFace> = when (facing) {
        BlockFace.NORTH, BlockFace.SOUTH -> listOf(BlockFace.EAST, BlockFace.WEST)
        BlockFace.EAST, BlockFace.WEST -> listOf(BlockFace.NORTH, BlockFace.SOUTH)
        else -> emptyList()
    }

    private fun Inventory.hasItemDeniedTo(player: Player): Boolean =
        contents.any { it != null && player.accessTo(it) != Access.ALLOW }
}

internal fun Player.playDenySound() = playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f)
