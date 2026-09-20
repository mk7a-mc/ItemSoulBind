package com.mk7a.soulbind.command

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import java.util.concurrent.CompletableFuture

/** The `slot` argument of /remotebinditem: a named equipment slot, a raw inventory index, or `all`. */
sealed interface SlotTarget {

    data object All : SlotTarget

    sealed interface Single : SlotTarget {
        fun itemIn(inventory: PlayerInventory): ItemStack?
    }

    data class Equipment(val slot: EquipmentSlot) : Single {
        override fun itemIn(inventory: PlayerInventory): ItemStack? = inventory.getItem(slot)
    }

    data class Index(val index: Int) : Single {
        override fun itemIn(inventory: PlayerInventory): ItemStack? = inventory.getItem(index)
    }
}

internal object SlotTargetArgument : CustomArgumentType.Converted<SlotTarget, String> {

    private val INDICES = 0..40

    private val named: Map<String, SlotTarget> = mapOf(
        "mainhand" to SlotTarget.Equipment(EquipmentSlot.HAND),
        "offhand" to SlotTarget.Equipment(EquipmentSlot.OFF_HAND),
        "helmet" to SlotTarget.Equipment(EquipmentSlot.HEAD),
        "chestplate" to SlotTarget.Equipment(EquipmentSlot.CHEST),
        "leggings" to SlotTarget.Equipment(EquipmentSlot.LEGS),
        "boots" to SlotTarget.Equipment(EquipmentSlot.FEET),
        "all" to SlotTarget.All,
    )

    private val invalidSlot = DynamicCommandExceptionType { input ->
        LiteralMessage("Unknown slot '$input': expected ${named.keys.joinToString()} or ${INDICES.first}-${INDICES.last}")
    }

    override fun getNativeType(): ArgumentType<String> = StringArgumentType.word()

    override fun convert(nativeType: String): SlotTarget =
        named[nativeType]
            ?: nativeType.toIntOrNull()?.takeIf { it in INDICES }?.let(SlotTarget::Index)
            ?: throw invalidSlot.create(nativeType)

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        named.keys.filter { it.startsWith(builder.remainingLowerCase) }.forEach(builder::suggest)
        return builder.buildFuture()
    }
}
