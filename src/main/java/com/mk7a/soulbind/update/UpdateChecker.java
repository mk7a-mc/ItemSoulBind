package com.mk7a.soulbind.update;

import org.bukkit.plugin.Plugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

public final class UpdateChecker {

    private static final URI LATEST_RELEASE = URI.create(
            "https://github.com/mk7a-mc/ItemSoulBind/releases/latest");

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private UpdateChecker() {}

    public static void check(Plugin plugin) {
        String currentTag = "v" + plugin.getDescription().getVersion();
        HttpRequest request = HttpRequest.newBuilder(LATEST_RELEASE)
                .method("HEAD", BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(5))
                .build();

        CLIENT.sendAsync(request, BodyHandlers.discarding())
                .thenApply(r -> r.headers().firstValue("location").orElseThrow())
                .thenApply(loc -> loc.substring(loc.lastIndexOf('/') + 1))
                .thenAccept(latestTag -> {
                    if (!latestTag.equals(currentTag)) {
                        announce(plugin, latestTag);
                    }
                })
                .exceptionally(e -> {
                    plugin.getLogger().warning("Could not check for updates.");
                    return null;
                });
    }

    private static void announce(Plugin plugin, String latestTag) {
        var log = plugin.getLogger();
        log.info("Latest release: " + latestTag + " (running " + plugin.getDescription().getVersion() + ")");
        log.info("Download: https://modrinth.com/plugin/itemsoulbind");
        log.info("Source:   https://github.com/mk7a-mc/ItemSoulBind/releases/latest");
    }
}
