package net.william278.papiproxybridge;

import com.google.inject.Inject;
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
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@Plugin(id = "papiproxybridge")
public class VelocityPAPIProxyBridge implements ProxyPAPIProxyBridge {

    private final Map<UUID, CompletableFuture<String>> requests;
    private final ChannelIdentifier channelIdentifier;
    private final ProxyServer server;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;

    @Inject
    public VelocityPAPIProxyBridge(ProxyServer server, org.slf4j.Logger logger, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.requests = new HashMap<>();
        this.channelIdentifier = new LegacyChannelIdentifier(getChannel());
    }

    @Subscribe
    public void onProxyInitialization(@NotNull ProxyInitializeEvent event) {
        // Register the plugin message channel
        server.getChannelRegistrar().register(getChannelIdentifier());

        // Register the plugin with the API
        PlaceholderAPI.register(this);

        // Setup metrics
        metricsFactory.make(this, 17878);

        logger.info("PAPIProxyBridge (" + server.getVersion().getName() + ") has been enabled!");
    }

    @Subscribe
    public void onPluginMessageReceived(@NotNull PluginMessageEvent event) {
        handlePluginMessage(this, event.getIdentifier().getId(), event.getData());
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        if (exceptions.length > 0) {
            logger.error(message, exceptions[0]);
        } else {
            logger.info(message);
        }
    }

    @Override
    @NotNull
    public Map<UUID, CompletableFuture<String>> getRequests() {
        return requests;
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull UUID uuid) {
        return server.getPlayer(uuid).map(VelocityUser::adapt);
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull String username) {
        return server.getPlayer(username).map(VelocityUser::adapt);
    }

    @NotNull
    public ChannelIdentifier getChannelIdentifier() {
        return channelIdentifier;
    }
}
