package com.mk7a.soulbind.util;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class BindUtil {

    private static NamespacedKey key;

    public static void setKey(NamespacedKey key) {
        BindUtil.key = key;
    }

    /**
     * Checks if item is soul bound to a player.
     * @param item Target item
     * @return boolean
     */
    public static boolean hasOwner(ItemStack item) {
        return DataContainerUtil.readContainerTag(item, key).isPresent();
    }

    /**
     * Checks for player access to an item. If not bound returns true.
     * If bound checks owner UUID and compares with player's.
     * @param item Target item
     * @param player Target player
     * @return boolean
     */
    public static boolean hasAccess(ItemStack item, Player player) {

        Optional<String> ownerUUID = DataContainerUtil.readContainerTag(item, key);
        return !ownerUUID.isPresent() || ownerUUID.get().equalsIgnoreCase(player.getUniqueId().toString());
    }


    public static String getOwnerUUID(ItemStack item) {

        return DataContainerUtil.readContainerTag(item, key).get();
    }

    public static ItemStack setOwner(ItemStack item, Player player) {

        String UUID = player.getUniqueId().toString();
        return DataContainerUtil.writeContainerTag(item, UUID, key);
    }


    public static ItemStack removeOwner(ItemStack item) {

        return DataContainerUtil.removeContainerTag(item, key);
    }
}
