package com.mk7a.soulbind.config

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.configuration.ConfigurationSection

class Messages(private val config: ConfigurationSection) {

    private val prefix = parse("msgPrefix")

    val denyPlayer = prefixed("denyMsg")
    val denyGroup = prefixed("denyMsgGroup")
    val detectedPlayer = prefixed("detectedItemMessage")
    val detectedGroup = prefixed("detectedItemMessageGroup")
    val bindSuccess = prefixed("bindSuccess")
    val bindErrorHeldItem = prefixed("bindErrorHeldItem")
    val bindErrorAlreadyBound = prefixed("bindErrorAlreadyBound")
    val bindErrorRemoteNoItem = prefixed("bindErrorRemoteNoItem")
    val bindErrorItemChanged = prefixed("bindErrorItemChanged")
    val unbindErrorNotBound = prefixed("unbindErrorNotBound")
    val unbindSuccess = prefixed("unbindSuccess")
    val craftDeny = prefixed("craftDeny")
    val enchantDeny = prefixed("enchantDeny")
    val anvilDeny = prefixed("anvilDeny")
    val smithingDeny = prefixed("smithingDeny")
    val foundItems = prefixed("foundItems")
    val noItems = prefixed("noItems")
    val specialBindDone = prefixed("specialBindDone")
    val commandBlocked = prefixed("cmdBlocked")
    val configReloaded = Component.textOfChildren(prefix, Component.text("Configuration Reloaded."))

    fun inventoryProcessed(player: String, count: Int): Component = prefixed(
        "inventoryProcessSuccess",
        Placeholder.unparsed("player", player),
        Placeholder.unparsed("count", count.toString()),
    )

    fun bookGiven(player: String): Component = prefixed("bookGiven", Placeholder.unparsed("player", player))

    fun bindLookup(player: String): Component = prefixed("bindLookup", Placeholder.unparsed("player", player))

    fun bindErrorNoSuchPlayer(player: String): Component =
        prefixed("bindErrorNoSuchPlayer", Placeholder.unparsed("player", player))

    fun detectedBroadcastPlayer(player: String): Component =
        parse("detectedItemBroadcast", Placeholder.unparsed("player", player))

    fun detectedBroadcastGroup(player: String): Component =
        parse("detectedItemBroadcastGroup", Placeholder.unparsed("player", player))

    private fun parse(key: String, vararg tags: TagResolver): Component =
        miniMessage.deserialize(config.getString(key).orEmpty(), *tags)

    // Wrapped in an unstyled parent so the prefix's formatting can't leak into the message
    private fun prefixed(key: String, vararg tags: TagResolver): Component =
        Component.textOfChildren(prefix, parse(key, *tags))
}
