package net.william278.papiproxybridge;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.user.BungeeUser;
import net.william278.papiproxybridge.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BungeePAPIProxyBridge extends Plugin implements ProxyPAPIProxyBridge, Listener {

    private final Map<UUID, CompletableFuture<String>> requests = new HashMap<>();

    @Override
    public void onEnable() {
        // Register the plugin message channel
        getProxy().registerChannel(getChannel());

        // Register the plugin message listener
        getProxy().getPluginManager().registerListener(this, this);

        // Register the plugin with the API
        PlaceholderAPI.register(this);
    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent event) {
        if (event.getSender() instanceof ProxiedPlayer sender) {
            BungeeUser.adapt(sender).handlePluginMessage(this, event.getTag(), event.getData());
        }
    }

    @Override
    @NotNull
    public Map<UUID, CompletableFuture<String>> getRequests() {
        return requests;
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(getProxy().getPlayer(uuid)).map(BungeeUser::adapt);
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        if (exceptions.length > 0) {
            getLogger().log(level, message, exceptions[0]);
        } else {
            getLogger().log(level, message);
        }
    }

}
