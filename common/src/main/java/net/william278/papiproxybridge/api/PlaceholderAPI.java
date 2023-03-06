package net.william278.papiproxybridge.api;

import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    private static final int PLACEHOLDER_REQUEST_TIMEOUT = 400;
    private static PlaceholderAPI instance;
    private final PAPIProxyBridge plugin;

    /**
     * <b>Internal only</b> - Create a new instance of the API
     *
     * @param plugin The plugin to register
     */
    private PlaceholderAPI(@NotNull PAPIProxyBridge plugin) {
        this.plugin = plugin;
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
        return plugin.createRequest(text, player)
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
