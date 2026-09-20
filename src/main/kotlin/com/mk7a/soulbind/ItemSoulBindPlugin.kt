package com.mk7a.soulbind

import com.mk7a.soulbind.bind.SoulBinder
import com.mk7a.soulbind.command.SoulBindCommands
import com.mk7a.soulbind.config.Settings
import com.mk7a.soulbind.itemreturn.ItemReturnService
import com.mk7a.soulbind.listener.CommandBlockerListener
import com.mk7a.soulbind.listener.ModificationListener
import com.mk7a.soulbind.listener.ProtectionListener
import com.mk7a.soulbind.listener.RegistrationListener
import com.mk7a.soulbind.listener.SoulBindBookListener
import com.mk7a.soulbind.update.UpdateChecker
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class ItemSoulBindPlugin : JavaPlugin() {

    // Read from every region thread under Folia
    @Volatile
    lateinit var settings: Settings
        private set

    private var itemReturn: ItemReturnService? = null

    override fun onEnable() {
        reloadSettings()

        val binder = SoulBinder(this)
        val itemReturn = ItemReturnService(this).also {
            it.start()
            itemReturn = it
        }

        listOf(
            RegistrationListener(binder),
            ProtectionListener(this),
            ModificationListener(this),
            CommandBlockerListener(this),
            SoulBindBookListener(this, binder),
            itemReturn,
        ).forEach { server.pluginManager.registerEvents(it, this) }

        val commands = SoulBindCommands(this, binder, itemReturn)
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands.register(it.registrar()) }

        UpdateChecker.check(this)
        Metrics(this, BSTATS_PLUGIN_ID)
    }

    override fun onDisable() {
        itemReturn?.stop()
    }

    fun reloadSettings() {
        saveDefaultConfig()
        reloadConfig()

        // An explicit fallback bypasses the bundled defaults, so a missing key counts as outdated
        if (config.getDouble(CONFIG_VERSION_PATH, 0.0) != CONFIG_VERSION) {
            logger.warning("Outdated config: default values will be used for anything missing.")
            logger.warning("Messages now use MiniMessage formatting; legacy '&' color codes are no longer translated.")
            logger.warning("Please re-generate config.yml or get the latest from Modrinth/GitHub.")
        }

        settings = Settings(config)
    }

    private companion object {
        const val BSTATS_PLUGIN_ID = 4271
        const val CONFIG_VERSION = 2.0
        const val CONFIG_VERSION_PATH = "configVersionDoNotModify"
    }
}
