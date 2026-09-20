package com.mk7a.soulbind.listener

import com.mk7a.soulbind.ItemSoulBindPlugin
import com.mk7a.soulbind.bind.isSoulBound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

/** Blocks configured commands (e.g. auction listings) while a soul bound item is held. */
class CommandBlockerListener(private val plugin: ItemSoulBindPlugin) : Listener {

    private val settings get() = plugin.settings

    @EventHandler(priority = EventPriority.HIGH)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val command = event.message.removePrefix("/")
        if (settings.blockedCommands.none { command.startsWith(it, ignoreCase = true) }) return

        val player = event.player
        if (player.inventory.itemInMainHand.isSoulBound) {
            event.isCancelled = true
            player.sendMessage(settings.messages.commandBlocked)
        }
    }
}
