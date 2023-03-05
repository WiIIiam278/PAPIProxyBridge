package net.william278.papiproxybridge;

import net.william278.papiproxybridge.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public interface PAPIProxyBridge {

    @NotNull
    default String getChannel() {
        return getChannelNamespace() + ":" + getChannelKey();
    }

    @NotNull
    String getChannelKey();

    @NotNull
    default String getChannelNamespace() {
        return "papiproxybridge";
    }

    Optional<OnlineUser> findPlayer(@NotNull UUID uuid);

    CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser user);

    void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions);

}