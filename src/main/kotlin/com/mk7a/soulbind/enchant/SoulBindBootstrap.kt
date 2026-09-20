package com.mk7a.soulbind.enchant

import com.mk7a.soulbind.config.miniMessage
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry.EnchantmentCost
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.tag.TagKey
import io.papermc.paper.tag.TagEntry
import net.kyori.adventure.key.Key
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemType

/**
 * Registers the soul bind book enchantment. Registries freeze before the plugin enables, so the
 * enchantment's config values are read here and only change on a server restart.
 */
@Suppress("UnstableApiUsage", "unused")
class SoulBindBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        // Missing on first start, in which case the defaults below apply
        val config = YamlConfiguration.loadConfiguration(context.dataDirectory.resolve("config.yml").toFile())
        val name = miniMessage.deserialize(config.getString("bookEnchantName", DEFAULT_NAME)!!)
        val anvilCost = config.getInt("bookAnvilCost", DEFAULT_ANVIL_COST).coerceAtLeast(0)

        val lifecycle = context.lifecycleManager

        // Anything can be soul bound, so the enchantment has to be supported on every item
        lifecycle.registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ITEM)) { event ->
            val allItems = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).keyStream()
                .map { TagEntry.valueEntry(TypedKey.create(RegistryKey.ITEM, it)) }
                .toList()
            event.registrar().setTag(BINDABLE, allItems)
        }

        lifecycle.registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler { event ->
            event.registry().register(SoulboundEnchantment.KEY) { builder ->
                builder.description(name)
                    .supportedItems(event.getOrCreateTag(BINDABLE))
                    .maxLevel(1)
                    .anvilCost(anvilCost)
                    .activeSlots(EquipmentSlotGroup.ANY)
                    // Unobtainable from the enchanting table, and worth no experience in a grindstone
                    .weight(1)
                    .minimumCost(EnchantmentCost.of(0, 0))
                    .maximumCost(EnchantmentCost.of(0, 0))
            }
        })
    }

    private companion object {
        const val DEFAULT_NAME = "Soulbound"
        const val DEFAULT_ANVIL_COST = 4
        val BINDABLE: TagKey<ItemType> = TagKey.create(RegistryKey.ITEM, Key.key("itemsoulbind", "bindable"))
    }
}
