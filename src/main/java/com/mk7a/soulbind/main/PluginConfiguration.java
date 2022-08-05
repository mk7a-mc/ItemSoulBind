package com.mk7a.soulbind.main;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

import static com.mk7a.soulbind.util.Util.color;

public class PluginConfiguration {

    public String prefix;
    public String registerString;
    public String groupRegisterString;
    public String loreMsg;
    public String loreMsgGroup;
    public String denyMsg;
    public String denyMsgGroup;
    public String detectedItemMessage;
    public String detectedItemMessageGroup;
    public String detectedItemBroadcast;
    public String detectedItemBroadcastGroup;
    public String noPermissionGeneric;
    public String noPermissionBindOthers;
    public String bindSuccess;
    public String bindErrorHeldItem;
    public String bindErrorAlreadyBound;
    public String bindErrorNoSuchPlayer;
    public String unbindErrorNotBound;
    public String unbindSuccess;
    public String inventoryProcessSuccess;
    public String craftDeny;
    public String enchantDeny;
    public String anvilDeny;
    public String foundItems;
    public String noItems;
    public String bindOnUseString;
    public String bindOnEquipString;
    public String bindOnPickupString;
    public String specialBindDone;
    public String cmdBlocked;

    public Boolean displayLore;
    public Boolean displayLoreGroup;
    public Boolean preventPlacing;
    public Boolean preventCraft;
    public Boolean preventEnchant;
    public Boolean preventAnvil;
    public Boolean consoleLogDetection;
    public Boolean bindOnUse;
    public Boolean bindOnEquip;
    public Boolean bindOnPickup;
    public Boolean disableEffects;
    public List<String> blockedCommands;


    private final ItemSoulBindPlugin plugin;

    protected PluginConfiguration(ItemSoulBindPlugin plugin) {
        this.plugin = plugin;

        getConfigValues();
    }


    protected void getConfigValues() {

        FileConfiguration config = plugin.getConfig();

        registerString = config.getString("registerString");
        groupRegisterString = config.getString("groupRegisterString");
        displayLore = config.getBoolean("displayLoreMsg");
        displayLoreGroup = config.getBoolean("displayLoreMsgGroup");
        loreMsg = color(config.getString("loreMsg"));
        loreMsgGroup = color(config.getString("loreMsgGroup"));
        prefix = color(config.getString("msgPrefix"));

        consoleLogDetection = config.getBoolean("consoleLogDetection");
        preventPlacing = config.getBoolean("preventPlacing");
        preventCraft = config.getBoolean("preventCraft");
        preventEnchant = config.getBoolean("preventEnchant");
        preventAnvil = config.getBoolean("preventAnvil");
        bindOnUse = config.getBoolean("bindOnUse");
        bindOnEquip = config.getBoolean("bindOnEquip");
        bindOnPickup = config.getBoolean("bindOnPickup");
        disableEffects = config.getBoolean("disableEffects");

        denyMsg = color(config.getString("denyMsg"));
        denyMsgGroup = color(config.getString("denyMsgGroup"));
        detectedItemMessage = color(config.getString("detectedItemMessage"));
        detectedItemMessageGroup = color(config.getString("detectedItemMessageGroup"));
        detectedItemBroadcast = color(config.getString("detectedItemBroadcast"));
        detectedItemBroadcastGroup = color(config.getString("detectedItemBroadcastGroup"));
        noPermissionGeneric = color(config.getString("noPermissionGeneric"));
        noPermissionBindOthers = color(config.getString("noPermissionBindOthers"));
        bindSuccess = color(config.getString("bindSuccess"));
        bindErrorHeldItem = color(config.getString("bindErrorHeldItem"));
        bindErrorAlreadyBound = color(config.getString("bindErrorAlreadyBound"));
        bindErrorNoSuchPlayer = color(config.getString("bindErrorNoSuchPlayer"));
        unbindErrorNotBound = color(config.getString("unbindErrorNotBound"));
        unbindSuccess = color(config.getString("unbindSuccess"));
        inventoryProcessSuccess = color(config.getString("inventoryProcessSuccess"));
        craftDeny = color(config.getString("craftDeny"));
        enchantDeny = color(config.getString("enchantDeny"));
        anvilDeny = color(config.getString("anvilDeny"));
        foundItems = color(config.getString("foundItems"));
        noItems = color(config.getString("noItems"));
        bindOnUseString = color(config.getString("bouString"));
        bindOnEquipString = color(config.getString("boeString"));
        bindOnPickupString = color(config.getString("bopString"));
        specialBindDone = color(config.getString("specialBindDone"));
        cmdBlocked = color(config.getString("cmdBlocked"));
        blockedCommands = config.getStringList("blockedCommands");
    }
}
