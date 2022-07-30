package com.mk7a.soulbind.util;

import com.mk7a.soulbind.main.ItemSoulBindPlugin;
import com.mk7a.soulbind.main.PluginPermissions;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Util {

    /**
     * Short chat message formatting method. Using '&' character for format codes
     *
     * @param i Input string
     * @return Formatted message
     */
    public static String color(String i) {
        return ChatColor.translateAlternateColorCodes('&', i);
    }


    /**
     * Do soul binding particle and sound effects on player, if feature not disabled in config.
     *
     * @param player Target player
     */
    public static void bindEffect(Player player) {

        if (!ItemSoulBindPlugin.getPluginConfig().disableEffects) {

            player.getWorld().spawnParticle(Particle.SPELL_WITCH,
                    player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5);

            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1, 2);
        }
    }


    /**
     * Send plugin message with prefix
     *
     * @param target  Target CommandSender
     * @param message Message content
     */
    public static void sendMessage(CommandSender target, String message) {
        target.sendMessage(ItemSoulBindPlugin.getPluginConfig().prefix + message);
    }



    public static boolean canIgnoreSoulBind(Player player) {

        return player.hasPermission(PluginPermissions.BYPASS) || player.getGameMode().equals(GameMode.CREATIVE);
    }
}
