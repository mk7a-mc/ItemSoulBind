package com.mk7a.soulbind.update

import org.bukkit.plugin.Plugin
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/** Resolves the latest release tag from GitHub's `releases/latest` redirect, without touching the API. */
object UpdateChecker {

    private val LATEST_RELEASE = URI.create("https://github.com/mk7a-mc/ItemSoulBind/releases/latest")
    private val TIMEOUT = Duration.ofSeconds(5)

    fun check(plugin: Plugin) {
        val log = plugin.logger
        val currentVersion = plugin.pluginMeta.version

        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(TIMEOUT)
            .build()
        val request = HttpRequest.newBuilder(LATEST_RELEASE)
            .method("HEAD", HttpRequest.BodyPublishers.noBody())
            .timeout(TIMEOUT)
            .build()

        // Completes on the HTTP client's own threads; only logging happens there
        client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .thenApply { response -> response.headers().firstValue("location").orElseThrow().substringAfterLast('/') }
            .thenAccept { latestTag ->
                if (latestTag != "v$currentVersion") {
                    log.info("Latest release: $latestTag (running $currentVersion)")
                    log.info("Download: https://modrinth.com/plugin/itemsoulbind")
                    log.info("Source:   $LATEST_RELEASE")
                }
            }
            .exceptionally { log.warning("Could not check for updates."); null }
            .whenComplete { _, _ -> client.shutdown() }
    }
}
