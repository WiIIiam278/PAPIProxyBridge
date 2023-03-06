package net.william278.papiproxybridge;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.user.OnlineUser;
import net.william278.papiproxybridge.user.VelocityUser;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Plugin(id = "papiproxybridge")
public class VelocityPAPIProxyBridge implements ProxyPAPIProxyBridge {

    private final Map<UUID, CompletableFuture<String>> requests;
    private final ChannelIdentifier channelIdentifier;
    private final ProxyServer proxyServer;
    private final Logger logger;

    @Inject
    public VelocityPAPIProxyBridge(@NotNull ProxyServer proxyServer, @NotNull Logger logger) {
        this.requests = new HashMap<>();
        this.channelIdentifier = new LegacyChannelIdentifier(getChannel());
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(@NotNull ProxyInitializeEvent event) {
        // Register the plugin message channel
        proxyServer.getChannelRegistrar().register(getChannelIdentifier());

        // Register the plugin with the API
        PlaceholderAPI.register(this);

        logger.info("PAPIProxyBridge has been enabled!");
    }

    @Subscribe
    public void onPluginMessageReceived(@NotNull PluginMessageEvent event) {
        handlePluginMessage(this, event.getIdentifier().getId(), event.getData());
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        if (exceptions.length > 0) {
            logger.log(level, message, exceptions[0]);
        } else {
            logger.log(level, message);
        }
    }

    @Override
    @NotNull
    public Map<UUID, CompletableFuture<String>> getRequests() {
        return requests;
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull UUID uuid) {
        return proxyServer.getPlayer(uuid).map(VelocityUser::adapt);
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull String username) {
        return proxyServer.getPlayer(username).map(VelocityUser::adapt);
    }

    @NotNull
    public ChannelIdentifier getChannelIdentifier() {
        return channelIdentifier;
    }
}
