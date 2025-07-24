package com.mk7a.soulbind.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public final class DataContainerUtil {

    static ItemStack writeContainerTag(ItemStack itemStack, String data, NamespacedKey key) {

        ItemMeta meta = itemStack.getItemMeta();
        itemStack.setItemMeta(writeContainerTag(meta, data, key));
        return itemStack;
    }

    private static ItemMeta writeContainerTag(ItemMeta meta, String data, NamespacedKey key) {

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, data);
        return meta;
    }

    static Optional<String> readContainerTag(ItemStack itemStack, NamespacedKey key) {

        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String tag = container.get(key, PersistentDataType.STRING);
        if (tag != null && tag.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(tag);
        }
    }

    static ItemStack removeContainerTag(ItemStack itemStack, NamespacedKey key) {

        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().remove(key);
        itemStack.setItemMeta(meta);
        return itemStack;
    }


}
