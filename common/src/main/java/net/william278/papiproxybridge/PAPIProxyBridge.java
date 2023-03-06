package net.william278.papiproxybridge;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.william278.papiproxybridge.user.OnlineUser;
import net.william278.papiproxybridge.user.Request;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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
    default String getChannelNamespace() {
        return "papiproxybridge";
    }

    @NotNull
    default String getChannelKey() {
        return "format";
    }

    Optional<OnlineUser> findPlayer(@NotNull UUID uuid);

    Optional<OnlineUser> findPlayer(@NotNull String username);

    @SuppressWarnings("UnstableApiUsage")
    default void handlePluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message) {
        plugin.log(Level.INFO, "Received plugin message from on channel " + channel);
        if (!channel.equals(plugin.getChannel())) {
            return;
        }

        final ByteArrayDataInput inputStream = ByteStreams.newDataInput(message);
        final String username = inputStream.readUTF();
        final OnlineUser user = plugin.findPlayer(username).orElse(null);
        if (user == null) {
            return;
        }

        short messageLength = inputStream.readShort();
        byte[] messageBody = new byte[messageLength];
        inputStream.readFully(messageBody);

        try (final DataInputStream messageReader = new DataInputStream(new ByteArrayInputStream(messageBody))) {
            final String read = messageReader.readUTF();
            plugin.log(Level.INFO, "Handling plugin message: " + read);
            user.handlePluginMessage(plugin, Request.fromString(read));
        } catch (Exception e) {
            plugin.log(Level.SEVERE, "Failed to fully read plugin message", e);
        }
    }

    CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser user);

    void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions);

}