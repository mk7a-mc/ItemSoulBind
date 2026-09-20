package com.mk7a.soulbind.config

import com.mk7a.soulbind.bind.SpecialBind
import org.bukkit.configuration.file.YamlConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Guards the bundled config.yml: every value must load and parse as MiniMessage. */
class DefaultConfigTest {

    private val config = javaClass.getResourceAsStream("/config.yml")!!.reader().use(YamlConfiguration::loadConfiguration)
    private val settings = Settings(config)

    @Test
    fun `messages are prefixed and free of unparsed tags`() {
        val messages = settings.messages

        assertEquals("[SoulBind] Soul bind complete.", messages.bindSuccess.toPlainText())
        assertEquals("[SoulBind] Processed inventory of Steve (3)", messages.inventoryProcessed("Steve", 3).toPlainText())
        assertEquals(
            "(Steve) was detected with an item soul bound to another player.",
            messages.detectedBroadcastPlayer("Steve").toPlainText(),
        )
        assertEquals("[SoulBind] Looking up Steve...", messages.bindLookup("Steve").toPlainText())
        assertEquals("[SoulBind] No player found for Steve.", messages.bindErrorNoSuchPlayer("Steve").toPlainText())
        assertFalse('<' in messages.foundItems.toPlainText())
    }

    @Test
    fun `every string value parses without leftover tags or legacy codes`() {
        val placeholders = Regex("<(username|group|player|count)>")
        config.getKeys(false)
            .mapNotNull { key -> config.getString(key)?.takeIf { config.isString(key) } }
            .filterNot { it.startsWith("%") } // register strings are plain text
            .forEach { raw ->
                val plain = miniMessage.deserialize(raw.replace(placeholders, "x")).toPlainText()
                assertFalse('<' in plain || '&' in plain, "Unparsed formatting in: $raw")
            }
    }

    @Test
    fun `special bind lines and register strings load`() {
        assertEquals(SpecialBind.entries.toSet(), settings.specialBindLore.keys)
        assertEquals("Soul binds on pickup", settings.specialBindLore.getValue(SpecialBind.ON_PICKUP).toPlainText())
        assertEquals("%soulbind%", settings.registerString)
        assertEquals("%soulbindgroup%", settings.groupRegisterString)
        assertTrue(settings.effectsEnabled)
        assertTrue(settings.groupLore.matches(settings.groupLore.render("vip")))
    }

    @Test
    fun `no special bind toggles remain`() {
        listOf("bindOnUse", "bindOnEquip", "bindOnPickup").forEach { assertFalse(config.contains(it), it) }
    }
}
