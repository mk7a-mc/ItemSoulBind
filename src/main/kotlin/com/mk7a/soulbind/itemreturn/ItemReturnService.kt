package com.mk7a.soulbind.itemreturn

import com.mk7a.soulbind.ItemSoulBindPlugin
import com.mk7a.soulbind.Permissions
import com.mk7a.soulbind.bind.Access
import com.mk7a.soulbind.bind.SoulBind
import com.mk7a.soulbind.bind.accessTo
import com.mk7a.soulbind.bind.ignoresSoulBind
import com.mk7a.soulbind.bind.soulBind
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Confiscates soul bound items that end up in the wrong player's inventory and holds them until
 * their owner collects them with /returnitems. Held items are persisted by [ReturnedItemsStore].
 *
 * Under Folia, items are confiscated and collected on the region threads of the players involved
 * and saved from the global one, so the held items are only ever swapped as immutable lists.
 */
class ItemReturnService(private val plugin: ItemSoulBindPlugin) : Listener {

    private val settings get() = plugin.settings
    private val store = ReturnedItemsStore(plugin)
    private val foundItems = ConcurrentHashMap<UUID, List<ItemStack>>(store.load())
    private val saveQueued = AtomicBoolean()

    fun start() {
        if (foundItems.isNotEmpty()) {
            plugin.logger.info("Holding ${foundItems.values.sumOf { it.size }} returned item(s) for ${foundItems.size} player(s)")
        }
        plugin.server.globalRegionScheduler.runAtFixedRate(plugin, { _ -> remindOwners() }, 1, REMINDER_PERIOD_TICKS)
    }

    fun stop() = store.close(foundItems)

    fun openReturnedItems(player: Player) {
        val gui = ReturnedItemsHolder().inventory
        var taken = emptyList<ItemStack>()
        foundItems.computeIfPresent(player.uniqueId) { _, items ->
            taken = items.take(gui.size)
            items.drop(gui.size).ifEmpty { null }
        }
        if (taken.isEmpty()) {
            player.sendMessage(settings.messages.noItems)
            return
        }

        taken.forEach { gui.addItem(it) }
        saveSoon()

        player.openInventory(gui)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        player.scheduler.runDelayed(plugin, { _ -> remind(player) }, null, 1)
    }

    /** Runs last and skips cancelled clicks, so items in other plugins' GUIs are never confiscated. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (player.ignoresSoulBind) return
        if ((event.clickedInventory as? PlayerInventory)?.holder != player) return

        val clickedItem = event.currentItem?.takeIf { it.hasItemMeta() }
        val cursorItem = event.cursor.takeIf { it.hasItemMeta() }
        val access = player.accessTo(clickedItem ?: cursorItem ?: return)
        if (access == Access.ALLOW) return

        if (cursorItem != null) {
            // The cursor item only lands in the inventory once the click is processed
            player.scheduler.run(plugin, { _ -> confiscateAll(player) }, null)
        } else if (clickedItem != null) {
            event.isCancelled = true
            player.inventory.setItem(event.slot, null)
            confiscate(player, clickedItem, access)
        }
    }

    @EventHandler
    fun onReturnedItemsClose(event: InventoryCloseEvent) {
        if (event.inventory.getHolder(false) !is ReturnedItemsHolder) return

        val player = event.player
        event.inventory.contents.filterNotNull().forEach { player.world.dropItemNaturally(player.location, it) }
    }

    private fun confiscateAll(player: Player) {
        val inventory = player.inventory
        for (slot in 0 until inventory.size) {
            val item = inventory.getItem(slot) ?: continue
            val access = player.accessTo(item)
            if (access != Access.ALLOW) {
                inventory.setItem(slot, null)
                confiscate(player, item, access)
            }
        }
    }

    private fun confiscate(holder: Player, item: ItemStack, access: Access) {
        val messages = settings.messages
        val bind = item.soulBind

        val (notice, broadcast) = if (bind is SoulBind.ToPlayer) {
            foundItems.merge(bind.owner, listOf(item)) { held, found -> held + found }
            saveSoon()
            holder.playSound(holder.location, Sound.BLOCK_NOTE_BLOCK_SNARE, 1f, 1f)
            messages.detectedPlayer to messages.detectedBroadcastPlayer(holder.name)
        } else {
            messages.detectedGroup to messages.detectedBroadcastGroup(holder.name)
        }

        holder.sendMessage(notice)
        if (settings.logDetections) plugin.componentLogger.info(broadcast)
        plugin.server.onlinePlayers.forEach { online ->
            online.onOwnThread { if (online.hasPermission(Permissions.NOTIFY)) online.sendMessage(broadcast) }
        }
    }

    /** Saves on the next tick, so a whole inventory confiscated at once is written once. */
    private fun saveSoon() {
        if (!saveQueued.compareAndSet(false, true)) return
        plugin.server.globalRegionScheduler.run(plugin) { _ ->
            // Cleared before the snapshot is taken, so a change that misses it queues the next save
            saveQueued.set(false)
            store.save(foundItems)
        }
    }

    private fun remindOwners() = foundItems.keys.mapNotNull(plugin.server::getPlayer)
        .forEach { owner -> owner.onOwnThread { remind(owner) } }

    private fun remind(player: Player) {
        if (foundItems.containsKey(player.uniqueId) && player.hasPermission(Permissions.RETURN_ITEMS)) {
            player.sendMessage(settings.messages.foundItems)
        }
    }

    /** Players other than the one an event is about may belong to another region's thread. */
    private fun Player.onOwnThread(action: () -> Unit) {
        if (plugin.server.isOwnedByCurrentRegion(this)) action() else scheduler.run(plugin, { _ -> action() }, null)
    }

    private companion object {
        const val REMINDER_PERIOD_TICKS = 20L * 60
    }
}
