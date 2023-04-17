/*
 * This file is part of PAPIProxyBridge, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.papiproxybridge.api;

import net.jodah.expiringmap.ExpiringMap;
import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * The main API for the ProxyPlaceholderAPI plugin
 * <p>
 * Use {@link #getInstance()} to get the instance of this class,
 * then use {@link #formatPlaceholders(String, OnlineUser)} to get a future that will supply the formatted text
 * <p>
 * Example:
 * <pre>{@code
 *     final PlaceholderAPI api = PlaceholderAPI.getInstance();
 *     final UUID player = player.getUniqueId();
 *     api.formatPlaceholders("Hello %player_name%!", player).thenAccept(formatted -> {
 *         player.sendMessage(formatted);
 *     });
 * }</pre>
 *
 * @author William278
 */
@SuppressWarnings("unused")
public final class PlaceholderAPI {

    private static PlaceholderAPI instance;
    private final PAPIProxyBridge plugin;
    private final Map<UUID, ExpiringMap<String, String>> cache;
    private long requestTimeout = 400;
    private long cacheExpiry = 30000;

    /**
     * <b>Internal only</b> - Create a new instance of the API
     *
     * @param plugin The plugin to register
     */
    private PlaceholderAPI(@NotNull PAPIProxyBridge plugin) {
        this.plugin = plugin;
        this.cache = new HashMap<>();
    }

    /**
     * Get the instance of the API. This is the entry point for the API
     *
     * @return The instance of the API
     * @since 1.0
     */
    @NotNull
    public static PlaceholderAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ProxyPlaceholderApi is not initialized");
        }
        return instance;
    }

    /**
     * <b>Internal only</b> - Register the plugin with the API
     *
     * @param plugin The plugin to register
     */
    public static void register(@NotNull PAPIProxyBridge plugin) {
        instance = new PlaceholderAPI(plugin);
    }

    /**
     * Format the text with the placeholders of the player
     * <p>
     * This method accepts the {@link UUID unique id} of a player who will be passed as the user for formatting the placeholders;
     * distinct from the player dispatching the plugin message across the network.
     *
     * @param text      The text to format
     * @param requester The player used to request the formatting
     * @param formatFor The player to format the text for
     * @return A future that will supply the formatted text
     * @since 1.2
     */
    public CompletableFuture<String> formatPlaceholders(@NotNull String text, @NotNull OnlineUser requester, @NotNull UUID formatFor) {
        if (cacheExpiry > 0 && cache.containsKey(formatFor) && cache.get(formatFor).containsKey(text)) {
            return CompletableFuture.completedFuture(cache.get(formatFor).get(text));
        }
        return plugin.createRequest(text, requester, formatFor)
                .thenApply(formatted -> {
                    cache.computeIfAbsent(requester.getUniqueId(), uuid -> ExpiringMap.builder()
                                    .expiration(cacheExpiry, TimeUnit.MILLISECONDS)
                                    .build())
                            .put(text, formatted);
                    return formatted;
                })
                .orTimeout(requestTimeout, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> text);
    }

    /**
     * Format the text with the placeholders of the player
     *
     * @param text   The text to format
     * @param player The player to format the text for
     * @return A future that will supply the formatted text
     * @since 1.0
     */
    public CompletableFuture<String> formatPlaceholders(@NotNull String text, @NotNull OnlineUser player) {
        return formatPlaceholders(text, player, player.getUniqueId());
    }

    /**
     * Format the text with the placeholders of the player
     * <p>
     * This method accepts the {@link UUID unique id} of a player who will be passed as the user for formatting the placeholders;
     * distinct from the player dispatching the plugin message across the network.
     *
     * @param text      The text to format
     * @param requester The {@link UUID unique id} of the player used to request the formatting. Note that this user must be online.
     * @param formatFor The {@link UUID unique id} of the player to format the text for
     * @return A future that will supply the formatted text
     * @throws IllegalArgumentException If the requesting player could not be resolved from their {@link UUID}
     * @since 1.2
     */
    public CompletableFuture<String> formatPlaceholders(@NotNull String text, @NotNull UUID requester, @NotNull UUID formatFor) {
        return formatPlaceholders(text, plugin.findPlayer(requester)
                .orElseThrow(() -> new IllegalArgumentException("Requesting player is not online")), formatFor);
    }

    /**
     * Format the text with the placeholders of the player
     *
     * @param text   The text to format
     * @param player The {@link UUID unique id} of the player to format the text for. Note that this user must be online.
     * @return A future that will supply the formatted text
     * @throws IllegalArgumentException If the player could not be resolved from their {@link UUID}
     * @since 1.0
     */
    public CompletableFuture<String> formatPlaceholders(@NotNull String text, @NotNull UUID player) throws IllegalArgumentException {
        return formatPlaceholders(text, plugin.findPlayer(player)
                .orElseThrow(() -> new IllegalArgumentException("Player not found")));
    }

    /**
     * Set the timeout for requesting formatting from the proxy in milliseconds.
     * If a request is not completed within this time, the original text will be returned
     * <p>
     * The default value is 400 milliseconds (0.4 seconds)
     *
     * @param requestTimeout The timeout for requesting formatting from the proxy in milliseconds
     * @throws IllegalArgumentException If the timeout is negative
     * @since 1.2
     */
    public void setRequestTimeout(long requestTimeout) {
        if (requestTimeout < 0) {
            throw new IllegalArgumentException("Request timeout cannot be negative");
        }
        this.requestTimeout = requestTimeout;
    }

    /**
     * Set the expiry time for the cache in milliseconds
     * <p>
     * The default value is 30000 milliseconds (30 seconds)
     *
     * @param cacheExpiry The expiry time for the cache in milliseconds
     * @throws IllegalArgumentException If the expiry time is negative
     * @since 1.2
     */
    public void setCacheExpiry(long cacheExpiry) {
        if (cacheExpiry < 0) {
            throw new IllegalArgumentException("Cache expiry cannot be negative");
        }
        this.cacheExpiry = cacheExpiry;
    }

}
