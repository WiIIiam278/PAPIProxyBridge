package net.william278.papiproxybridge;

import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.papi.Formatter;
import net.william278.papiproxybridge.user.BukkitUser;
import net.william278.papiproxybridge.user.OnlineUser;
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
        getServer().getMessenger().registerOutgoingPluginChannel(this, OnlineUser.BUNGEE_CHANNEL_ID);
        getServer().getMessenger().registerIncomingPluginChannel(this, OnlineUser.BUNGEE_CHANNEL_ID, this);

        // Register the plugin with the API
        PlaceholderAPI.register(this);
    }

    @Override
    @NotNull
    public String getChannelKey() {
        return "response";
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(getServer().getPlayer(uuid)).map(BukkitUser::adapt);
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
        BukkitUser.adapt(player).handlePluginMessage(this, channel, message);
    }

    @NotNull
    public final String formatPlaceholders(@NotNull BukkitUser user, @NotNull String text) {
        return formatter.formatPlaceholders(user.getPlayer(), text);
    }

}