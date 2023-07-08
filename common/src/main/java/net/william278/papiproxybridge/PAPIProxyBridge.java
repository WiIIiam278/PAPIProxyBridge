/*
 * This file is part of PAPIProxyBridge, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.papiproxybridge;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.william278.papiproxybridge.user.OnlineUser;
import net.william278.papiproxybridge.user.Request;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public interface PAPIProxyBridge {

    String HANDSHAKE_PLACEHOLDER = "%papiproxybridge_handshake%";
    String HANDSHAKE_RESPONSE = "confirmed";

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

    @NotNull
    List<? extends OnlineUser> getOnlineUsers();

    Optional<OnlineUser> findPlayer(@NotNull UUID uuid);

    Optional<OnlineUser> findPlayer(@NotNull String username);

    default void handlePluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message) {
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
            user.handlePluginMessage(plugin, Request.fromString(messageReader.readUTF()));
        } catch (Exception e) {
            plugin.log(Level.SEVERE, "Failed to fully read plugin message", e);
        }
    }

    CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser requester, @NotNull UUID formatFor);

    CompletableFuture<List<String>> findServers();

    void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions);

}