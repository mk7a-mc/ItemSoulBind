package com.mk7a.soulbind.main;

import com.mk7a.soulbind.commands.CommandsModule;
import com.mk7a.soulbind.itemreturn.ItemReturnModule;
import com.mk7a.soulbind.listeners.CommandBlockerListener;
import com.mk7a.soulbind.listeners.ItemModificationListener;
import com.mk7a.soulbind.listeners.ItemProtectionListener;
import com.mk7a.soulbind.listeners.ItemRegistrationListener;
import com.mk7a.soulbind.util.BindUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;


public final class ItemSoulBindPlugin extends JavaPlugin {

    // Do not modify or backwards compatibility will break
    public NamespacedKey key = new NamespacedKey(this, "SoulBindUUID");

    private static final double CONFIG_VER = 1.4;
    private static final String VERSION_PATH = "configVersionDoNotModify";

    static protected PluginConfiguration pluginConfig;

    @Override
    public void onEnable() {

        updateCheck();

        BindUtil.setKey(key);

        pluginConfig = new PluginConfiguration(this);
        reloadPluginConfig();

        setupListeners();

        new CommandsModule(this).setup();

        new ItemReturnModule(this).setup();

        int pluginID = 4271;
        new Metrics(this, pluginID);

    }

    private void setupListeners() {
        new ItemRegistrationListener(this).register();
        new ItemProtectionListener(this).register();
        new ItemModificationListener(this).register();
        new CommandBlockerListener(this).register();
    }

    public void reloadPluginConfig() {

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        reloadConfig();
        boolean validConfig = this.validateConfigVersion();
        if (!validConfig) {
            Bukkit.getLogger().info("=================================================================================");
            this.getLogger().warning(" Warning: outdated config. Some default values will be used.");
            this.getLogger().warning(" Please re-generate or get the latest config from spigot.");
            Bukkit.getLogger().info("=================================================================================");
        }

        pluginConfig.getConfigValues();

    }

    private boolean validateConfigVersion() {
        return CONFIG_VER == getConfig().getDouble(VERSION_PATH);
    }

    private void updateCheck() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                InputStream resource = new URL("https://api.spigotmc.org/legacy/update.php?resource=64541").openStream();
                Scanner scanner = new Scanner(resource);
                if (scanner.hasNext()) {
                    String latestVersion = scanner.nextLine();
                    String pluginVersion = getDescription().getVersion();
                    if (!latestVersion.equalsIgnoreCase(pluginVersion)) {
                        getLogger().info("An update is available");
                        getLogger().info("https://www.spigotmc.org/resources/itemsoulbind.64541/");
                    }
                }
            } catch (IOException e) {
                getLogger().warning("Could not check for updates.");
            }
        });
    }

    @Override
    public void onDisable() {
        //
    }

    public static PluginConfiguration getPluginConfig() {
        return pluginConfig;
    }
}