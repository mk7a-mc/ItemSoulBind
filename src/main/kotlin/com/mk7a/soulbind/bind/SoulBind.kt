package com.mk7a.soulbind.bind

import com.mk7a.soulbind.Permissions
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

sealed interface SoulBind {

    data class ToPlayer(val owner: UUID) : SoulBind

    /** [permission] is the full node, i.e. `itemsoulbind.group.<group>`. */
    data class ToGroup(val permission: String) : SoulBind

    companion object {
        fun toGroup(group: String) = ToGroup(Permissions.GROUP_PREFIX + group)
    }
}

enum class Access { ALLOW, DENY_PLAYER, DENY_GROUP }

// Do not modify or backwards compatibility will break
private val PLAYER_KEY = NamespacedKey("itemsoulbind", "soulbinduuid")
private val GROUP_KEY = NamespacedKey("itemsoulbind", "soulbindgroup")

/** The item's bind, stored in its persistent data. Assign `null` to remove it. */
var ItemStack.soulBind: SoulBind?
    get() {
        if (isEmpty) return null
        val data = persistentDataContainer
        val owner = data.get(PLAYER_KEY, PersistentDataType.STRING)?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        if (owner != null) return SoulBind.ToPlayer(owner)
        return data.get(GROUP_KEY, PersistentDataType.STRING)?.takeIf { it.isNotEmpty() }?.let(SoulBind::ToGroup)
    }
    set(value) {
        editPersistentDataContainer { data ->
            data.remove(PLAYER_KEY)
            data.remove(GROUP_KEY)
            when (value) {
                is SoulBind.ToPlayer -> data.set(PLAYER_KEY, PersistentDataType.STRING, value.owner.toString())
                is SoulBind.ToGroup -> data.set(GROUP_KEY, PersistentDataType.STRING, value.permission)
                null -> {}
            }
        }
    }

val ItemStack.isSoulBound: Boolean get() = soulBind != null

fun Player.accessTo(item: ItemStack): Access = when (val bind = item.soulBind) {
    null -> Access.ALLOW
    is SoulBind.ToPlayer -> if (bind.owner == uniqueId) Access.ALLOW else Access.DENY_PLAYER
    is SoulBind.ToGroup -> if (hasPermission(bind.permission)) Access.ALLOW else Access.DENY_GROUP
}

val Player.ignoresSoulBind: Boolean
    get() = hasPermission(Permissions.BYPASS) || gameMode == GameMode.CREATIVE
