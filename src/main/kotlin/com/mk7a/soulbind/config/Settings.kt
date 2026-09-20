package com.mk7a.soulbind.config

import com.mk7a.soulbind.bind.SpecialBind
import net.kyori.adventure.text.Component
import org.bukkit.configuration.ConfigurationSection

/** Immutable snapshot of config.yml; replaced wholesale on reload. */
class Settings(config: ConfigurationSection) {

    val registerString: String = config.getString("registerString").orEmpty()
    val groupRegisterString: String = config.getString("groupRegisterString").orEmpty()

    val specialBindLore: Map<SpecialBind, Component> = SpecialBind.entries.associateWith {
        miniMessage.deserialize(config.getString(it.configKey).orEmpty()).asLoreLine()
    }

    val effectsEnabled = !config.getBoolean("disableEffects")

    val displayPlayerLore = config.getBoolean("displayLoreMsg")
    val playerLore = LoreTemplate(config.getString("loreMsg").orEmpty(), "username")
    val displayGroupLore = config.getBoolean("displayLoreMsgGroup")
    val groupLore = LoreTemplate(config.getString("loreMsgGroup").orEmpty(), "group")

    val grindstoneUnbinds = config.getBoolean("grindstoneUnbinds", false)
    val showContainerDeny = config.getBoolean("showContainerDeny", true)
    val logDetections = config.getBoolean("consoleLogDetection")
    val preventPlacing = config.getBoolean("preventPlacing")
    val preventCraft = config.getBoolean("preventCraft")
    val preventEnchant = config.getBoolean("preventEnchant")
    val preventAnvil = config.getBoolean("preventAnvil")
    val preventSmithing = config.getBoolean("preventSmithing")
    val blockedCommands: List<String> = config.getStringList("blockedCommands")

    val messages = Messages(config)
}
