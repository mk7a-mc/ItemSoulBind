package com.mk7a.soulbind.bind

import com.mk7a.soulbind.ItemSoulBindPlugin
import com.mk7a.soulbind.config.toPlainText
import com.mk7a.soulbind.enchant.SoulboundEnchantment
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

/** Applies and removes binds, keeping the item's lore in step with the configured display options. */
class SoulBinder(private val plugin: ItemSoulBindPlugin) {

    private val settings get() = plugin.settings

    fun bindToPlayer(item: ItemStack, owner: Player) = bindToPlayer(item, owner.uniqueId, owner.name)

    /** The owner does not need to be online, or to have ever joined. */
    fun bindToPlayer(item: ItemStack, ownerId: UUID, ownerName: String) {
        if (settings.displayPlayerLore) item.loreLines += settings.playerLore.render(ownerName)
        item.soulBind = SoulBind.ToPlayer(ownerId)
    }

    fun bindToGroup(item: ItemStack, group: String) {
        if (settings.displayGroupLore) item.loreLines += settings.groupLore.render(group)
        item.soulBind = SoulBind.toGroup(group)
    }

    /**
     * Binds [item] if its lore carries the group register string, the register string, or one of the
     * [special] bind lines. The triggering line is replaced by the bind lore message, or removed if
     * that is disabled.
     *
     * @return whether the item was bound
     */
    fun bindFromLore(item: ItemStack, player: Player, vararg special: SpecialBind): Boolean {
        if (item.isEmpty || item.isSoulBound) return false
        val lore = item.loreLines.ifEmpty { return false }
        val text = lore.map { it.toPlainText() }

        val groupString = settings.groupRegisterString
        val groupLine = if (groupString.isEmpty()) -1 else text.indexOfFirst { groupString in it }
        val group = text.getOrNull(groupLine)?.substringAfter(groupString)?.substringBefore(' ')
        if (!group.isNullOrEmpty()) {
            item.loreLines = lore.replaceLine(groupLine, settings.groupLore.render(group).takeIf { settings.displayGroupLore })
            item.soulBind = SoulBind.toGroup(group)
            return true
        }

        val triggers = (special.map { settings.specialBindLore.getValue(it).toPlainText() } + settings.registerString)
            .filter { it.isNotEmpty() }
        val triggerLine = text.indexOfFirst { line -> triggers.any { it in line } }
        if (triggerLine < 0) return false

        item.loreLines = lore.replaceLine(triggerLine, settings.playerLore.render(player.name).takeIf { settings.displayPlayerLore })
        item.soulBind = SoulBind.ToPlayer(player.uniqueId)
        return true
    }

    fun applySpecialBind(item: ItemStack, special: SpecialBind) {
        item.loreLines += settings.specialBindLore.getValue(special)
    }

    fun unbind(item: ItemStack) {
        val lore = item.loreLines
        val bindLine = lore.indexOfFirst { settings.playerLore.matches(it) || settings.groupLore.matches(it) }
        if (bindLine >= 0) item.loreLines = lore.replaceLine(bindLine, null)
        item.soulBind = null
        SoulboundEnchantment.removeFrom(item)
    }

    fun playBindEffect(player: Player) {
        if (!settings.effectsEnabled) return
        player.world.spawnParticle(Particle.WITCH, player.location.add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5)
        player.playSound(player.location, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1f, 2f)
    }
}

private var ItemStack.loreLines: List<Component>
    get() = getData(DataComponentTypes.LORE)?.lines().orEmpty()
    set(value) = if (value.isEmpty()) resetData(DataComponentTypes.LORE) else setData(DataComponentTypes.LORE, ItemLore.lore(value))

private fun List<Component>.replaceLine(index: Int, replacement: Component?): List<Component> =
    toMutableList().apply { if (replacement != null) set(index, replacement) else removeAt(index) }
