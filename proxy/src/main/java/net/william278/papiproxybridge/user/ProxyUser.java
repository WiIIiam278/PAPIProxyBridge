package net.william278.papiproxybridge.user;

import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.ProxyPAPIProxyBridge;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface ProxyUser extends OnlineUser {

    @Override
    default void handlePluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message) {
        ((ProxyPAPIProxyBridge) plugin).getRequests()
                .getOrDefault(message.getUuid(), new CompletableFuture<>())
                .complete(message.getMessage());
    }

}
