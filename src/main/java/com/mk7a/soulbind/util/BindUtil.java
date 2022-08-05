package com.mk7a.soulbind.util;

import com.mk7a.soulbind.listeners.Access;
import com.mk7a.soulbind.main.PluginPermissions;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class BindUtil {

    private static NamespacedKey bindKey;
    private static NamespacedKey groupBindKey;

    public static void setKeys(NamespacedKey bindKey, NamespacedKey groupBindKey) {
        BindUtil.bindKey = bindKey;
        BindUtil.groupBindKey = groupBindKey;
    }

    /**
     * Checks if item is soul bound to a player or group permission.
     *
     * @param item Target item
     * @return boolean
     */
    public static boolean hasBind(ItemStack item) {
        return DataContainerUtil.readContainerTag(item, bindKey).isPresent()
                || DataContainerUtil.readContainerTag(item, groupBindKey).isPresent();
    }

    /**
     * Checks for player access to an item.
     *
     * @param item   Target item
     * @param player Target player
     * @return boolean
     */
    public static Access getAccessLevel(ItemStack item, Player player) {

        if (hasPlayerOwner(item)) {
            String ownerUUID = getPlayerOwner(item);
            if (ownerUUID.equals(player.getUniqueId().toString())) {
                return Access.ALLOW;
            } else {
                return Access.DENY_PLAYER;
            }
        }

        if (hasGroupOwner(item)) {
            String groupOwnerString = getGroupOwner(item);
            if (player.hasPermission(groupOwnerString)) {
                return Access.ALLOW;
            } else {
                return Access.DENY_GROUP;
            }
        }

        return Access.ALLOW;

    }

    public static boolean hasPlayerOwner(ItemStack item) {
        return DataContainerUtil.readContainerTag(item, bindKey).isPresent();
    }

    public static String getPlayerOwner(ItemStack item) {
        return DataContainerUtil.readContainerTag(item, bindKey).get();
    }

    public static boolean hasGroupOwner(ItemStack item) {
        return DataContainerUtil.readContainerTag(item, groupBindKey).isPresent();
    }

    public static String getGroupOwner(ItemStack item) {
        return DataContainerUtil.readContainerTag(item, groupBindKey).get();
    }

    public static ItemStack setPlayerOwner(ItemStack item, Player player) {

        String UUID = player.getUniqueId().toString();
        return DataContainerUtil.writeContainerTag(item, UUID, bindKey);
    }

    public static ItemStack setGroupOwner(ItemStack item, String groupPermission) {

        return DataContainerUtil.writeContainerTag(item, formatCompleteGroupPerm(groupPermission), groupBindKey);
    }


    public static ItemStack removeAnyOwner(ItemStack item) {

        item = DataContainerUtil.removeContainerTag(item, bindKey);
        return DataContainerUtil.removeContainerTag(item, groupBindKey);
    }

    private static String formatCompleteGroupPerm(String groupPerm) {
        return PluginPermissions.GROUP_BIND_ROOT + groupPerm;
    }

}
