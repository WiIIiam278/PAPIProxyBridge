package net.william278.papiproxybridge.user;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.william278.papiproxybridge.PAPIProxyBridge;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.logging.Level;

public interface OnlineUser {

    String BUNGEE_CHANNEL_ID = "BungeeCord";
    String FORWARD_TO_PLAYER = "ForwardToPlayer";

    @NotNull
    String getUsername();

    void sendPluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message);

    @SuppressWarnings("UnstableApiUsage")
    default void sendPluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request request) {
        final ByteArrayDataOutput messageWriter = ByteStreams.newDataOutput();
        messageWriter.writeUTF(FORWARD_TO_PLAYER);
        messageWriter.writeUTF(getUsername());
        messageWriter.writeUTF(plugin.getChannel());

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

        this.sendPluginMessage(plugin, BUNGEE_CHANNEL_ID, messageWriter.toByteArray());
    }

    @SuppressWarnings("UnstableApiUsage")
    default void handlePluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message) {
        if (!channel.equals(BUNGEE_CHANNEL_ID)) {
            return;
        }

        final ByteArrayDataInput inputStream = ByteStreams.newDataInput(message);
        final String subChannelId = inputStream.readUTF();
        if (!subChannelId.equals(plugin.getChannel())) {
            return;
        }

        short messageLength = inputStream.readShort();
        byte[] messageBody = new byte[messageLength];
        inputStream.readFully(messageBody);

        try (final DataInputStream messageReader = new DataInputStream(new ByteArrayInputStream(messageBody))) {
            handlePluginMessage(plugin, Request.fromString(messageReader.readUTF()));
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Failed to fully read plugin message", e);
        }
    }

    void handlePluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message);

}
