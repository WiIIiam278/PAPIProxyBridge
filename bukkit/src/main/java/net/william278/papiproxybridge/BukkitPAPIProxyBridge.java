package net.william278.papiproxybridge;

import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.papi.Formatter;
import net.william278.papiproxybridge.user.BukkitUser;
import net.william278.papiproxybridge.user.OnlineUser;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BukkitPAPIProxyBridge extends JavaPlugin implements PAPIProxyBridge, PluginMessageListener {
    private Formatter formatter;

    @Override
    public void onLoad() {
        // Initialize the formatter
        formatter = new Formatter();
    }

    @Override
    public void onEnable() {
        // Register the plugin message channel
        getServer().getMessenger().registerOutgoingPluginChannel(this, getChannel());
        getServer().getMessenger().registerIncomingPluginChannel(this, getChannel(), this);

        // Register the plugin with the API
        PlaceholderAPI.register(this);

        // Metrics
        new Metrics(this, 17880);

        getLogger().info("PAPIProxyBridge (" + getServer().getName() + ") has been enabled!");
    }

    @Override
    public void onDisable() {
        // Unregister the plugin message channel
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(getServer().getPlayer(uuid)).map(BukkitUser::adapt);
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull String username) {
        return Optional.ofNullable(getServer().getPlayerExact(username)).map(BukkitUser::adapt);
    }

    @Override
    public CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser user) {
        return CompletableFuture.completedFuture(formatPlaceholders((BukkitUser) user, text));
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        if (exceptions.length > 0) {
            getLogger().log(level, message, exceptions[0]);
        } else {
            getLogger().log(level, message);
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        log(Level.INFO, "Received plugin message from " + player.getName() + " on channel " + channel);
        this.handlePluginMessage(this, channel, message);
    }

    @NotNull
    public final String formatPlaceholders(@NotNull BukkitUser user, @NotNull String text) {
        return formatter.formatPlaceholders(user.getPlayer(), text);
    }

}