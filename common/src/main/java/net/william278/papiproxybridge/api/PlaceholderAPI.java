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

import com.google.common.collect.Maps;
import net.jodah.expiringmap.ExpiringMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.user.OnlineUser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;

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
    private static PAPIProxyBridge plugin;
    private final ConcurrentMap<UUID, ExpiringMap<String, String>> cache;
    private final ConcurrentMap<UUID, ExpiringMap<String, Component>> componentCache;
    private long requestTimeout = 1000;
    private long cacheExpiry = 30000;
    private int retryTimes = 3;

    /**
     * <b>Internal only</b> - Create a new instance of the API
     */
    @ApiStatus.Internal
    private PlaceholderAPI() {
        this.cache = Maps.newConcurrentMap();
        this.componentCache = Maps.newConcurrentMap();
    }

    /**
     * Get the instance of the API. This is an entry point for the API.
     * <p>
     * Shares expiration settings with all other plugins using this instance.
     * Prefer {@link #createInstance()} for unique instance customization.
     *
     * @return An instance of the API
     * @apiNote From version 1.3, #getInstance() will return a new instance of the PlaceholderAPI rather than a singleton
     * @since 1.0
     * @deprecated Use {@link #createInstance()} instead
     */
    @Deprecated(since = "1.3")
    @NotNull
    public static PlaceholderAPI getInstance() {
        return createInstance();
    }

    /**
     * Create a new instance of PlaceholderAPI allowing unique customization of caching mechanisms
     *
     * @return PlaceholderAPI instance that can be used to format text
     * @since 1.3
     */
    @NotNull
    public static PlaceholderAPI createInstance() {
        return new PlaceholderAPI();
    }

    /**
     * <b>Internal only</b> - Register the plugin with the API
     *
     * @param plugin The plugin to register
     */
    @ApiStatus.Internal
    public static void register(@NotNull PAPIProxyBridge plugin) {
        PlaceholderAPI.plugin = plugin;
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
    public CompletableFuture<String> formatPlaceholders(@NotNull String text, @NotNull OnlineUser requester,
                                                        @NotNull UUID formatFor) {
        return formatPlaceholders(text, requester, formatFor, retryTimes);
    }

    private CompletableFuture<String> formatPlaceholders(@NotNull String text, @NotNull OnlineUser requester,
                                                         @NotNull UUID formatFor, int times) {
        if (!requester.isConnected()) {
            return CompletableFuture.completedFuture(text);
        }
        if (cacheExpiry > 0 && cache.containsKey(formatFor) && cache.get(formatFor).containsKey(text)) {
            return CompletableFuture.completedFuture(cache.get(formatFor).get(text));
        }
        return plugin.createRequest(text, requester, formatFor, false, requestTimeout)
                .thenApply(formatted -> {
                    cache.computeIfAbsent(requester.getUniqueId(), uuid -> ExpiringMap.builder()
                                    .expiration(cacheExpiry, TimeUnit.MILLISECONDS)
                                    .build())
                            .put(text, formatted);
                    return formatted;
                })
                .orTimeout(requestTimeout, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    if (!requester.isConnected()) {
                        return text;
                    }
                    if (times > 0) {
                        try {
                            return formatPlaceholders(text, requester, formatFor, times - 1).get();
                        } catch (InterruptedException | ExecutionException e) {
                            plugin.log(Level.WARNING, "Failed to format placeholders for " + requester.getUsername(), e);
                            return text;
                        }
                    }
                    if (Arrays.stream(throwable.getSuppressed()).anyMatch(TimeoutException.class::isInstance)) {
                        plugin.log(Level.WARNING, "Timed out formatting placeholders for " + requester.getUsername() +
                                " timeout: " + requestTimeout + "ms");
                    } else {
                        plugin.log(Level.WARNING, "Failed to format placeholders for " + requester.getUsername(), throwable);
                    }
                    return text;
                });
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
     * @return A future that will supply the formatted text. If the requester is not online, the original text will be returned
     * @since 1.2
     */
    public CompletableFuture<String> formatPlaceholders(@NotNull String text, @NotNull UUID requester,
                                                        @NotNull UUID formatFor) {
        return plugin.findPlayer(requester)
                .map(onlineRequester -> formatPlaceholders(text, onlineRequester, formatFor))
                .orElse(CompletableFuture.completedFuture(text));
    }

    /**
     * Format the text with the placeholders of the player
     *
     * @param text   The text to format
     * @param player The {@link UUID unique id} of the player to format the text for. Note that this user must be online.
     * @return A future that will supply the formatted text. If the player is not online, the original text will be returned
     * @since 1.0
     */
    public CompletableFuture<String> formatPlaceholders(@NotNull String text, @NotNull UUID player) {
        return plugin.findPlayer(player)
                .map(requester -> formatPlaceholders(text, requester, player))
                .orElse(CompletableFuture.completedFuture(text));
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
     * @return A future that will supply the formatted component
     * @since 1.4
     */
    public CompletableFuture<Component> formatComponentPlaceholders(@NotNull String text, @NotNull OnlineUser requester,
                                                                    @NotNull UUID formatFor) {
        return formatComponentPlaceholders(text, requester, formatFor, retryTimes);
    }

    private CompletableFuture<Component> formatComponentPlaceholders(@NotNull String text, @NotNull OnlineUser requester,
                                                                     @NotNull UUID formatFor, int times) {
        if (!requester.isConnected()) {
            return CompletableFuture.completedFuture(Component.text(text));
        }
        if (cacheExpiry > 0 && componentCache.containsKey(formatFor) && componentCache.get(formatFor).containsKey(text)) {
            return CompletableFuture.completedFuture(componentCache.get(formatFor).get(text));
        }
        return plugin.createRequest(text, requester, formatFor, true, requestTimeout)
                .thenApply(formatted -> {
                    final Component deserialized = GsonComponentSerializer.gson().deserializeOr(formatted, Component.text(formatted));
                    componentCache.computeIfAbsent(requester.getUniqueId(), uuid -> ExpiringMap.builder()
                                    .expiration(cacheExpiry, TimeUnit.MILLISECONDS)
                                    .build())
                            .put(text, deserialized);
                    return deserialized;
                })
                .orTimeout(requestTimeout, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    if (!requester.isConnected()) {
                        return Component.text(text);
                    }
                    if (times > 0) {
                        try {
                            return formatComponentPlaceholders(text, requester, formatFor, times - 1).get();
                        } catch (InterruptedException | ExecutionException e) {
                            plugin.log(Level.WARNING, "Failed to format placeholders for " + requester.getUsername(), e);
                            return Component.text(text);
                        }
                    }
                    if (Arrays.stream(throwable.getSuppressed()).anyMatch(TimeoutException.class::isInstance)) {
                        plugin.log(Level.WARNING, "Timed out formatting placeholders for " + requester.getUsername() +
                                " timeout: " + requestTimeout + "ms");
                    } else {
                        plugin.log(Level.WARNING, "Failed to format placeholders for " + requester.getUsername(), throwable);
                    }
                    return Component.text(text);
                });
    }

    /**
     * Format the text with the placeholders of the player
     *
     * @param text   The text to format
     * @param player The player to format the text for
     * @return A future that will supply the formatted component
     * @since 1.4
     */
    public CompletableFuture<Component> formatComponentPlaceholders(@NotNull String text, @NotNull OnlineUser player) {
        return formatComponentPlaceholders(text, player, player.getUniqueId());
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
     * @return A future that will supply the formatted component. If the requester is not online, the original text will be returned
     * @since 1.4
     */
    public CompletableFuture<Component> formatComponentPlaceholders(@NotNull String text, @NotNull UUID requester,
                                                                    @NotNull UUID formatFor) {
        return plugin.findPlayer(requester)
                .map(onlineRequester -> formatComponentPlaceholders(text, onlineRequester, formatFor))
                .orElse(CompletableFuture.completedFuture(Component.text(text)));
    }

    /**
     * Format the text with the placeholders of the player
     *
     * @param text   The text to format
     * @param player The {@link UUID unique id} of the player to format the text for. Note that this user must be online.
     * @return A future that will supply the formatted component. If the player is not online, the original text will be returned
     * @since 1.4
     */
    public CompletableFuture<Component> formatComponentPlaceholders(@NotNull String text, @NotNull UUID player) {
        return plugin.findPlayer(player)
                .map(requester -> formatComponentPlaceholders(text, requester, player))
                .orElse(CompletableFuture.completedFuture(Component.text(text)));
    }


    /**
     * Fetch the list of backend servers with PAPIProxyBridge installed
     *
     * @return A future that will supply the list of backend servers
     * @throws UnsupportedOperationException If this method is called from a backend (Bukkit, Fabric) server
     * @apiNote This method can only be used from the proxy; it will throw an exception if called from a backend server
     * @since 1.3
     */
    public CompletableFuture<Set<String>> findServers() throws UnsupportedOperationException {
        return plugin.findServers(requestTimeout);
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

    /**
     * Returns the timeout for requesting formatting from the proxy in milliseconds.
     * If a request is not completed within this time, the original text will be returned.
     *
     * @return The timeout in milliseconds
     * @since 1.2
     */
    public long getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Returns the expiry time for the cache in milliseconds.
     *
     * @return The expiry time for the cache in milliseconds
     * @since 1.2
     */
    public long getCacheExpiry() {
        return cacheExpiry;
    }

    /**
     * Set the number of times to retry formatting placeholders if the request times out
     * <p>
     * The default value is 3
     *
     * @param retryTimes The number of times to retry formatting placeholders if the request times out
     * @throws IllegalArgumentException If the number of retries is negative
     * @since 1.6
     */
    public void setRetryTimes(int retryTimes) {
        if (retryTimes < 0) {
            throw new IllegalArgumentException("Retry times cannot be negative");
        }
        this.retryTimes = retryTimes;
    }

    /**
     * Returns the number of times to retry formatting placeholders if the request times out
     *
     * @return The number of times to retry formatting placeholders if the request times out
     * @since 1.6
     */
    public int getRetryTimes() {
        return retryTimes;
    }
}
