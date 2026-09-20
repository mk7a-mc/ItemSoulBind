package com.mk7a.soulbind.config

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

/**
 * A MiniMessage lore line with a single placeholder, e.g. `<blue>Soul bound to <username>`.
 */
class LoreTemplate(private val template: String, private val placeholder: String) {

    // Plain text either side of the placeholder, used to recognise rendered lines again
    private val prefix: String
    private val suffix: String

    init {
        val parts = render(SENTINEL).toPlainText().split(SENTINEL, limit = 2)
        prefix = parts[0]
        suffix = parts.getOrElse(1) { "" }
    }

    fun render(value: String): Component =
        miniMessage.deserialize(template, Placeholder.unparsed(placeholder, value)).asLoreLine()

    /** Whether [line] was rendered from this template, regardless of the placeholder value. */
    fun matches(line: Component): Boolean {
        if (prefix.isEmpty() && suffix.isEmpty()) return false
        val text = line.toPlainText()
        return text.length >= prefix.length + suffix.length && text.startsWith(prefix) && text.endsWith(suffix)
    }

    private companion object {
        const val SENTINEL = ""
    }
}
