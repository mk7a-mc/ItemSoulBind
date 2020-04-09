package com.mk7a.soulbind.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public final class DataContainerUtil {

    protected static ItemStack writeContainerTag(ItemStack itemStack, String data, NamespacedKey key) {

        ItemMeta meta = itemStack.getItemMeta();
        itemStack.setItemMeta(writeContainerTag(meta, data, key));
        return itemStack;
    }

    protected static ItemMeta writeContainerTag(ItemMeta meta, String data, NamespacedKey key) {

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, data);
        return meta;
    }

    protected static Optional<String> readContainerTag(ItemStack itemStack, NamespacedKey key) {

        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return Optional.ofNullable(container.get(key, PersistentDataType.STRING));
    }

    protected static ItemStack removeContainerTag(ItemStack itemStack, NamespacedKey key) {

        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().remove(key);
        itemStack.setItemMeta(meta);
        return itemStack;
    }


}
