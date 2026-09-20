package com.mk7a.soulbind.command

import com.destroystokyo.paper.profile.PlayerProfile
import com.mk7a.soulbind.ItemSoulBindPlugin
import com.mk7a.soulbind.Permissions
import com.mk7a.soulbind.bind.SoulBinder
import com.mk7a.soulbind.bind.SpecialBind
import com.mk7a.soulbind.bind.isSoulBound
import com.mk7a.soulbind.enchant.SoulboundEnchantment
import com.mk7a.soulbind.itemreturn.ItemReturnService
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

private typealias Context = CommandContext<CommandSourceStack>

private const val MAX_NAME_LENGTH = 16

internal class SoulBindCommands(
    private val plugin: ItemSoulBindPlugin,
    private val binder: SoulBinder,
    private val itemReturn: ItemReturnService,
) {
    private val messages get() = plugin.settings.messages

    fun register(commands: Commands) {
        commands.register("binditem", "Soul bind the held item, to yourself or another player") {
            requires(permission(Permissions.BIND))
            executes { ctx -> bindHeldItem(ctx, owner = ctx.source.playerOrThrow) }
            then(
                // Name or UUID rather than a selector, so the owner doesn't have to be online
                Commands.argument("player", StringArgumentType.word())
                    .requires(permission(Permissions.BIND_OTHERS))
                    .suggests(::suggestOnlinePlayers)
                    .executes(::bindHeldItemToOther)
            )
        }

        commands.register("bindall", "Bind all items in inventory") {
            requires(permission(Permissions.BIND_ALL))
            executes { ctx ->
                val player = ctx.source.playerOrThrow
                bindAll(player)
                binder.playBindEffect(player)
                ctx.reply(messages.bindSuccess)
            }
        }

        commands.register("unbinditem", "Remove soul bind protection from the held item") {
            requires(permission(Permissions.UNBIND))
            executes(::unbindHeldItem)
        }

        commands.register("groupbinditem", "Bind the held item to all players with permission ${Permissions.GROUP_PREFIX}<group>") {
            requires(permission(Permissions.GROUP_BIND))
            then(Commands.argument("group", StringArgumentType.word()).executes(::groupBindHeldItem))
        }

        commands.register("remotebinditem", "Remotely bind an item in a player's inventory") {
            requires(permission(Permissions.REMOTE_BIND))
            then(
                Commands.argument("player", ArgumentTypes.player())
                    .then(Commands.argument("slot", SlotTargetArgument).executes(::remoteBind))
            )
        }

        commands.register("bindinvitems", "Scan a player's inventory and bind any items with a register string in the lore") {
            requires(permission(Permissions.BIND_INV_ITEMS))
            then(Commands.argument("player", ArgumentTypes.player()).executes(::bindInventoryFromLore))
        }

        SpecialBind.entries.forEach { special ->
            commands.register(special.commandName, "Set the held item to soul bind ${special.name.lowercase().replace('_', ' ')}") {
                requires(permission(special.permission))
                executes { ctx -> applySpecialBind(ctx, special) }
            }
        }

        commands.register("soulbindbook", "Give a soul bind book, which binds an item to whoever combines it on in an anvil") {
            requires(permission(Permissions.BOOK_GIVE))
            executes { ctx -> giveBook(ctx, ctx.source.playerOrThrow) }
            then(Commands.argument("player", ArgumentTypes.player()).executes { ctx -> giveBook(ctx, ctx.player("player")) })
        }

        commands.register("returnitems", "Get found soul bound items back") {
            requires(permission(Permissions.RETURN_ITEMS))
            executes { ctx ->
                itemReturn.openReturnedItems(ctx.source.playerOrThrow)
                Command.SINGLE_SUCCESS
            }
        }

        commands.register("isb-reload", "Reload the ItemSoulBind config") {
            requires(permission(Permissions.ADMIN))
            executes { ctx ->
                plugin.reloadSettings()
                ctx.reply(messages.configReloaded)
            }
        }
    }

    private fun bindHeldItem(ctx: Context, owner: Player): Int {
        val holder = ctx.source.playerOrThrow
        val item = ctx.unboundHeldItem(holder) ?: return 0

        binder.bindToPlayer(item, owner)
        binder.playBindEffect(holder)
        return ctx.reply(messages.bindSuccess)
    }

    /**
     * Binds to a player given by name or UUID. Online players and the server's profile cache are
     * checked first; anyone else is looked up with Mojang, off the main thread.
     */
    private fun bindHeldItemToOther(ctx: Context): Int {
        val sender = ctx.source.sender
        val holder = ctx.source.playerOrThrow
        val item = ctx.unboundHeldItem(holder) ?: return 0
        val input = StringArgumentType.getString(ctx, "player")
        val profile = ownerProfile(input) ?: return ctx.fail(messages.bindErrorNoSuchPlayer(input))

        if (profile.completeFromCache()) {
            bindToProfile(holder, item, profile)
            return ctx.reply(messages.bindSuccess)
        }

        ctx.reply(messages.bindLookup(input))
        val snapshot = item.clone()
        plugin.server.asyncScheduler.runNow(plugin) { _ ->
            runCatching { profile.complete(false) }
                .onFailure { plugin.logger.warning("Profile lookup for '$input' failed: $it") }

            holder.scheduler.run(plugin, { _ ->
                // The holder has had time to swap items since the command ran
                val held = holder.inventory.itemInMainHand
                val result = when {
                    profile.id == null -> messages.bindErrorNoSuchPlayer(input)
                    held != snapshot -> messages.bindErrorItemChanged
                    else -> messages.bindSuccess.also { bindToProfile(holder, held, profile) }
                }
                sender.sendMessage(result)
            }, null)
        }
        return Command.SINGLE_SUCCESS
    }

    private fun bindToProfile(holder: Player, item: ItemStack, owner: PlayerProfile) {
        val ownerId = checkNotNull(owner.id)
        // A UUID that neither the cache nor Mojang can name is still bound, shown by its UUID
        binder.bindToPlayer(item, ownerId, owner.name?.ifEmpty { null } ?: ownerId.toString())
        binder.playBindEffect(holder)
    }

    private fun ownerProfile(input: String): PlayerProfile? {
        val uuid = runCatching { UUID.fromString(input) }.getOrNull()
        return when {
            uuid != null -> plugin.server.createProfile(uuid)
            input.length <= MAX_NAME_LENGTH -> plugin.server.createProfile(input)
            else -> null
        }
    }

    private fun suggestOnlinePlayers(ctx: Context, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val viewer = ctx.source.sender as? Player
        val prefix = builder.remainingLowerCase
        plugin.server.onlinePlayers
            .filter { viewer == null || viewer.canSee(it) }
            .map { it.name }
            .filter { it.lowercase().startsWith(prefix) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }

    private fun groupBindHeldItem(ctx: Context): Int {
        val holder = ctx.source.playerOrThrow
        val item = ctx.unboundHeldItem(holder) ?: return 0

        binder.bindToGroup(item, StringArgumentType.getString(ctx, "group"))
        binder.playBindEffect(holder)
        return ctx.reply(messages.bindSuccess)
    }

    private fun unbindHeldItem(ctx: Context): Int {
        val holder = ctx.source.playerOrThrow
        val item = ctx.heldItem(holder) ?: return 0
        if (!item.isSoulBound) return ctx.fail(messages.unbindErrorNotBound)

        binder.unbind(item)
        holder.playSound(holder.location, Sound.ENTITY_ITEM_BREAK, 1f, 1f)
        return ctx.reply(messages.unbindSuccess)
    }

    private fun applySpecialBind(ctx: Context, special: SpecialBind): Int {
        val item = ctx.unboundHeldItem(ctx.source.playerOrThrow) ?: return 0

        binder.applySpecialBind(item, special)
        return ctx.reply(messages.specialBindDone)
    }

    private fun giveBook(ctx: Context, target: Player): Int = onThreadOf(target) {
        // Whatever doesn't fit in the inventory is dropped at the target's feet
        target.inventory.addItem(SoulboundEnchantment.book()).values
            .forEach { target.world.dropItem(target.location, it) }
        ctx.reply(messages.bookGiven(target.name))
    }

    private fun remoteBind(ctx: Context): Int {
        val target = ctx.player("player")
        val slot = ctx.getArgument("slot", SlotTarget::class.java)

        return onThreadOf(target) {
            when (slot) {
                SlotTarget.All -> bindAll(target)
                is SlotTarget.Single -> {
                    val item = slot.itemIn(target.inventory)
                    if (item == null || item.isEmpty) return@onThreadOf ctx.fail(messages.bindErrorRemoteNoItem)
                    if (item.isSoulBound) return@onThreadOf ctx.fail(messages.bindErrorAlreadyBound)
                    binder.bindToPlayer(item, target)
                }
            }
            binder.playBindEffect(target)
            ctx.reply(messages.bindSuccess)
        }
    }

    private fun bindInventoryFromLore(ctx: Context): Int {
        val target = ctx.player("player")

        return onThreadOf(target) {
            val bound = target.inventory.contents.count { it != null && binder.bindFromLore(it, target, SpecialBind.ON_PICKUP) }

            if (bound > 0) binder.playBindEffect(target)
            ctx.reply(messages.inventoryProcessed(target.name, bound))
        }
    }

    /**
     * Runs [action] on the thread that owns [target]. That is the calling thread unless the server
     * is Folia and the target is in another region than the sender, or the sender is the console;
     * the command then counts as a success and [action] reports its outcome a tick later.
     */
    private fun onThreadOf(target: Player, action: () -> Int): Int {
        if (plugin.server.isOwnedByCurrentRegion(target)) return action()

        target.scheduler.run(plugin, { _ -> action() }, null)
        return Command.SINGLE_SUCCESS
    }

    private fun bindAll(player: Player) {
        player.inventory.contents
            .filterNotNull()
            .filterNot { it.isEmpty || it.isSoulBound }
            .forEach { binder.bindToPlayer(it, player) }
    }

    private fun Context.heldItem(holder: Player): ItemStack? =
        holder.inventory.itemInMainHand.takeUnless { it.isEmpty }
            ?: fail(messages.bindErrorHeldItem).let { null }

    private fun Context.unboundHeldItem(holder: Player): ItemStack? {
        val item = heldItem(holder) ?: return null
        return item.takeUnless { it.isSoulBound } ?: fail(messages.bindErrorAlreadyBound).let { null }
    }
}

private fun Commands.register(name: String, description: String, build: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit) {
    register(Commands.literal(name).apply(build).build(), description)
}

private fun permission(node: String) = Predicate<CommandSourceStack> { it.sender.hasPermission(node) }

private fun Context.player(argument: String): Player =
    getArgument(argument, PlayerSelectorArgumentResolver::class.java).resolve(source).first()

private fun Context.reply(message: Component): Int {
    source.sender.sendMessage(message)
    return Command.SINGLE_SUCCESS
}

private fun Context.fail(message: Component): Int {
    source.sender.sendMessage(message)
    return 0
}
