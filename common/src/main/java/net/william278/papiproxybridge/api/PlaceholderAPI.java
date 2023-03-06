package net.william278.papiproxybridge.api;

import net.jodah.expiringmap.ExpiringMap;
import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

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
    // The timeout for requesting formatting from the proxy in milliseconds
    private static final int PLACEHOLDER_REQUEST_TIMEOUT = 400;
    // The expiry time for the cache in milliseconds
    private static final int PLACEHOLDER_CACHE_EXPIRY = 30000;
    private static PlaceholderAPI instance;
    private final PAPIProxyBridge plugin;
    private final ExpiringMap<String, String> cache;

    /**
     * <b>Internal only</b> - Create a new instance of the API
     *
     * @param plugin The plugin to register
     */
    private PlaceholderAPI(@NotNull PAPIProxyBridge plugin) {
        this.plugin = plugin;
        this.cache = ExpiringMap.builder()
                .expiration(PLACEHOLDER_CACHE_EXPIRY, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * Get the instance of the API. This is the entry point for the API
     *
     * @return The instance of the API
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
     *
     * @param text   The text to format
     * @param player The player to format the text for
     * @return A future that will supply the formatted text
     */
    public CompletableFuture<String> formatPlaceholders(@NotNull String text, @NotNull OnlineUser player) {
        if (cache.containsKey(text)) {
            return CompletableFuture.completedFuture(cache.get(text));
        }
        return plugin.createRequest(text, player)
                .thenApply(formatted -> {
                    cache.put(text, formatted);
                    return formatted;
                })
                .orTimeout(PLACEHOLDER_REQUEST_TIMEOUT, java.util.concurrent.TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> text);
    }

    /**
     * Format the text with the placeholders of the player
     *
     * @param text   The text to format
     * @param player The {@link UUID unique id} of the player to format the text for
     * @return A future that will supply the formatted text
     * @throws IllegalArgumentException If the player could not be resolved from their {@link UUID}
     */
    public CompletableFuture<String> formatPlaceholders(@NotNull String text, @NotNull UUID player) throws IllegalArgumentException {
        return formatPlaceholders(text, plugin.findPlayer(player)
                .orElseThrow(() -> new IllegalArgumentException("Player not found")));
    }

}
