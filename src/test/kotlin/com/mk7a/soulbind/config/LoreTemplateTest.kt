package com.mk7a.soulbind.config

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoreTemplateTest {

    private val template = LoreTemplate("<blue>Soul bound to <username>", "username")

    @Test
    fun `renders the placeholder without lore italics`() {
        val line = template.render("Steve")

        assertEquals("Soul bound to Steve", line.toPlainText())
        assertEquals(TextDecoration.State.FALSE, line.decoration(TextDecoration.ITALIC))
    }

    @Test
    fun `placeholder value is not parsed as MiniMessage`() {
        assertEquals("Soul bound to <red>Steve", template.render("<red>Steve").toPlainText())
    }

    @Test
    fun `matches rendered lines whatever the placeholder value`() {
        assertTrue(template.matches(template.render("Steve")))
        assertTrue(template.matches(Component.text("Soul bound to Alex")))
        assertFalse(template.matches(Component.text("Sharpness V")))
    }

    @Test
    fun `matches text on both sides of the placeholder`() {
        val wrapped = LoreTemplate("[<group>] only", "group")

        assertTrue(wrapped.matches(wrapped.render("vip")))
        assertFalse(wrapped.matches(Component.text("[vip] and more")))
        assertFalse(wrapped.matches(Component.text("[")))
    }

    @Test
    fun `a bare placeholder matches nothing`() {
        assertFalse(LoreTemplate("<username>", "username").matches(Component.text("anything")))
    }
}
