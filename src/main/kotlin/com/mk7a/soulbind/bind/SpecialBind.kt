package com.mk7a.soulbind.bind

import com.mk7a.soulbind.Permissions

/** Deferred binds: the item carries a lore line and is bound to whoever first meets the condition. */
enum class SpecialBind(val commandName: String, val permission: String, val configKey: String) {
    ON_USE("bindonuse", Permissions.BIND_ON_USE, "bouString"),
    ON_EQUIP("bindonequip", Permissions.BIND_ON_EQUIP, "boeString"),
    ON_PICKUP("bindonpickup", Permissions.BIND_ON_PICKUP, "bopString"),
}
