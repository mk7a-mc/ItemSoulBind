package com.mk7a.soulbind.config

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

internal val miniMessage: MiniMessage = MiniMessage.miniMessage()

internal fun Component.toPlainText(): String = PlainTextComponentSerializer.plainText().serialize(this)

/** Lore renders italic by default; only keep it when the format asks for it. */
internal fun Component.asLoreLine(): Component =
    decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
