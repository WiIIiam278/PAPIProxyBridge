package net.william278.papiproxybridge.user;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.william278.papiproxybridge.PAPIProxyBridge;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

public interface OnlineUser {

    @NotNull
    String getUsername();

    void sendPluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message);

    @SuppressWarnings("UnstableApiUsage")
    default void sendPluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request request) {
        final ByteArrayDataOutput messageWriter = ByteStreams.newDataOutput();
        messageWriter.writeUTF(getUsername()); // Username

        // Write the plugin message
        try (final ByteArrayOutputStream messageByteStream = new ByteArrayOutputStream()) {
            try (DataOutputStream messageDataStream = new DataOutputStream(messageByteStream)) {
                messageDataStream.writeUTF(request.toString());
                messageWriter.writeShort(messageByteStream.toByteArray().length);
                messageWriter.write(messageByteStream.toByteArray());
            }
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Exception dispatching plugin message", e);
            return;
        }

        plugin.log(Level.INFO, "Sending plugin message: " + request);
        this.sendPluginMessage(plugin, plugin.getChannel(), messageWriter.toByteArray());
    }

    void handlePluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message);

}
