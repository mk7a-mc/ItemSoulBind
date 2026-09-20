package com.mk7a.soulbind.itemreturn

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.Base64
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level

/**
 * Keeps confiscated items in returned-items.yml so they survive a restart. Items are stored per
 * owner as Base64 of the server's own item format, which carries a data version and is upgraded on
 * load after a Minecraft update.
 */
internal class ReturnedItemsStore(private val plugin: Plugin) {

    private val file: Path = plugin.dataPath.resolve("returned-items.yml")

    // A single thread, so snapshots reach the disk in the order they were taken
    private val writer = Executors.newSingleThreadExecutor { task ->
        Thread(task, "ItemSoulBind-returned-items").apply { isDaemon = true }
    }

    fun load(): Map<UUID, List<ItemStack>> {
        val items = mutableMapOf<UUID, List<ItemStack>>()
        if (Files.notExists(file)) return items

        val yaml = YamlConfiguration()
        try {
            yaml.load(file.toFile())
        } catch (e: Exception) {
            // Set aside rather than left in place, where the next save would overwrite it
            val aside = file.resolveSibling("returned-items.broken-${System.currentTimeMillis()}.yml")
            Files.move(file, aside)
            plugin.logger.log(Level.SEVERE, "Could not read returned items; the file was kept as ${aside.fileName}", e)
            return items
        }

        for (key in yaml.getKeys(false)) {
            val owner = runCatching { UUID.fromString(key) }.getOrNull()
            if (owner == null) {
                plugin.logger.warning("Skipping returned items stored under '$key', which is not a UUID")
                continue
            }
            val stacks = yaml.getStringList(key).mapNotNull { encoded ->
                runCatching { ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded)) }
                    .onFailure { plugin.logger.log(Level.WARNING, "Skipping an unreadable returned item of $owner", it) }
                    .getOrNull()
            }
            if (stacks.isNotEmpty()) items[owner] = stacks
        }
        return items
    }

    /** Snapshots [items] on the calling thread and writes the snapshot off it. */
    fun save(items: Map<UUID, List<ItemStack>>) {
        val snapshot = serialize(items)
        writer.execute { write(snapshot) }
    }

    /** Lets queued writes finish, then writes [items] on the calling thread. For shutdown. */
    fun close(items: Map<UUID, List<ItemStack>>) {
        writer.shutdown()
        if (!writer.awaitTermination(SHUTDOWN_WAIT_SECONDS, TimeUnit.SECONDS)) {
            plugin.logger.warning("Timed out waiting for returned items to be written")
        }
        write(serialize(items))
    }

    /** @return the file's contents, or null when there is nothing left to store */
    private fun serialize(items: Map<UUID, List<ItemStack>>): String? {
        if (items.isEmpty()) return null

        val yaml = YamlConfiguration()
        items.forEach { (owner, stacks) ->
            yaml.set(owner.toString(), stacks.map { Base64.getEncoder().encodeToString(it.serializeAsBytes()) })
        }
        return yaml.saveToString()
    }

    private fun write(contents: String?) {
        try {
            if (contents == null) {
                Files.deleteIfExists(file)
                return
            }
            // Written beside the real file and moved over it, so a crash mid-write can't truncate it
            Files.createDirectories(file.parent)
            val temp = file.resolveSibling("${file.fileName}.tmp")
            Files.writeString(temp, contents)
            Files.move(temp, file, ATOMIC_MOVE, REPLACE_EXISTING)
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not save returned items", e)
        }
    }

    private companion object {
        const val SHUTDOWN_WAIT_SECONDS = 5L
    }
}
