package net.william278.papiproxybridge;

import net.william278.papiproxybridge.user.OnlineUser;
import net.william278.papiproxybridge.user.Request;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface ProxyPAPIProxyBridge extends PAPIProxyBridge {

    @NotNull
    Map<UUID, CompletableFuture<String>> getRequests();

    default CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser user) {
        final Request request = new Request(text);
        final CompletableFuture<String> future = new CompletableFuture<>();
        getRequests().put(request.getUuid(), future);
        future.orTimeout(800, TimeUnit.MILLISECONDS).exceptionally(throwable -> {
            getRequests().remove(request.getUuid());
            return text;
        });
        user.sendPluginMessage(this, request);
        return future;
    }

}
