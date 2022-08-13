package com.mk7a.soulbind.util;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Optional;

public final class BindStringUtil {

    private static final String USERNAME_PLACEHOLDER = "%username%";
    private static final String GROUP_PLACEHOLDER = "%group%";

    public static Optional<ItemStack> bindIfContainsString(Player player, ItemStack item, String... requiredString) {

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int loreLine = 0; loreLine < lore.size(); loreLine++) {

                String lineText = ChatColor.stripColor(lore.get(loreLine));

                for (String reqStrOption : requiredString) {
                    reqStrOption = ChatColor.stripColor(reqStrOption);

                    if (lineText.contains(reqStrOption)) {
                        ItemStack boundItem = applyLoreTriggeredBind(player, item, loreLine);
                        item.setItemMeta(boundItem.getItemMeta());
                        Util.bindEffect(player);

                        return Optional.of(boundItem);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<ItemStack> bindIfContainsGroupString(Player player, ItemStack item) {

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 0; i < lore.size(); i++) {

                String line = lore.get(i);
                String lineText = ChatColor.stripColor(line);

                String groupRegString = ChatColor.stripColor(ItemSoulBindPlugin.getPluginConfig().groupRegisterString);
                if (lineText.contains(groupRegString)) {

                    String permissionSuffix = lineText.split(groupRegString)[1].split(" ")[0];
                    ItemStack regItem = applyGroupBind(permissionSuffix, item, i);

                    item.setItemMeta(regItem.getItemMeta());
                    Util.bindEffect(player);

                    return Optional.of(regItem);


                }
            }
        }
        return Optional.empty();
    }


    private static ItemStack applyLoreTriggeredBind(Player player, ItemStack item, int lineIndex) {

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        if (ItemSoulBindPlugin.getPluginConfig().displayLore) {
            lore.set(lineIndex, ItemSoulBindPlugin.getPluginConfig().loreMsg.replaceAll(USERNAME_PLACEHOLDER, player.getName()));
        } else {
            lore.remove(lineIndex);
        }
        meta.setLore(lore);

        item.setItemMeta(meta);

        return BindUtil.setPlayerOwner(item, player);
    }

    private static ItemStack applyGroupBind(String permission, ItemStack item, int lineIndex) {

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        if (ItemSoulBindPlugin.getPluginConfig().displayLore) {
            lore.set(lineIndex, ItemSoulBindPlugin.getPluginConfig().loreMsgGroup.replaceAll(GROUP_PLACEHOLDER, permission));
        } else {
            lore.remove(lineIndex);
        }
        meta.setLore(lore);

        item.setItemMeta(meta);

        return BindUtil.setGroupOwner(item, permission);
    }
}
