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

package net.william278.papiproxybridge.user;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.william278.papiproxybridge.PAPIProxyBridge;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public interface OnlineUser {

    @NotNull
    String getUsername();

    @NotNull
    UUID getUniqueId();

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

        this.sendPluginMessage(plugin, plugin.getChannel(), messageWriter.toByteArray());
    }

    void handlePluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message);

}
