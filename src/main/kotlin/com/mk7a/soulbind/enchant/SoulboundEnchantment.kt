package com.mk7a.soulbind.enchant

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.keys.EnchantmentKeys
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

/**
 * The enchantment carried by soul bind books. It has no effect of its own: the bind itself still
 * lives in the item's persistent data, and is applied when the book is combined in an anvil.
 */
object SoulboundEnchantment {

    val KEY: TypedKey<Enchantment> = EnchantmentKeys.create(Key.key("itemsoulbind", "soulbound"))

    // Registered by SoulBindBootstrap, so only resolvable once the server's registries have loaded
    private val enchantment: Enchantment by lazy {
        RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(KEY)
    }

    fun book(): ItemStack = ItemStack.of(Material.ENCHANTED_BOOK).apply {
        setData(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantments.itemEnchantments().add(enchantment, 1).build())
    }

    fun isOn(item: ItemStack): Boolean = item.containsEnchantment(enchantment)

    fun addTo(item: ItemStack) = item.addUnsafeEnchantment(enchantment, 1)

    fun removeFrom(item: ItemStack) {
        item.removeEnchantment(enchantment)
    }
}
