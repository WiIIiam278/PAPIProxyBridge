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
import de.exlll.configlib.YamlConfigurations;
import net.william278.papiproxybridge.config.Settings;
import net.william278.papiproxybridge.messenger.Messenger;
import net.william278.papiproxybridge.user.OnlineUser;
import net.william278.papiproxybridge.user.Request;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public interface PAPIProxyBridge {

    String HANDSHAKE_PLACEHOLDER = "%papiproxybridge_handshake%";
    String HANDSHAKE_RESPONSE = "confirmed";
    String NAMESPACE = "papiproxybridge";
    String REQUEST = "request";
    String RESPONSE = "response";
    String FORMAT_CHANNEL = "format";
    String COMPONENT_CHANNEL = "component";

    @NotNull
    static String getChannel(boolean isRequest) {
        return getChannelNamespace() + ":" + subChannel(isRequest);
    }

    @NotNull
    static String getComponentChannel(boolean isRequest) {
        return getChannelNamespace() + ":" + subComponentChannel(isRequest);
    }

    static String subChannel(boolean isRequest) {
        return getChannelKey() + "-" + (isRequest ? REQUEST : RESPONSE);
    }

    static String subComponentChannel(boolean isRequest) {
        return getComponentChannelKey() + "-" + (isRequest ? REQUEST : RESPONSE);
    }

    @NotNull
    static String getChannelNamespace() {
        return NAMESPACE;
    }

    @NotNull
    static String getChannelKey() {
        return FORMAT_CHANNEL;
    }

    @NotNull
    static String getComponentChannelKey() {
        return COMPONENT_CHANNEL;
    }

    default String getLoadMessage() {
        return "PAPIProxyBridge (" + getServerType() + " " + getVersion() + ") has been enabled!";
    }

    String getServerType();

    default String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @NotNull
    Collection<? extends OnlineUser> getOnlineUsers();

    Optional<? extends OnlineUser> findPlayer(@NotNull UUID uuid);

    default void handleMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message, boolean isRequest) {
        if (!channel.equals(PAPIProxyBridge.getChannel(isRequest)) && !channel.equals(getComponentChannel(isRequest))) {
            return;
        }

        final ByteArrayDataInput inputStream = ByteStreams.newDataInput(message);
        final long mostSignificantBits = inputStream.readLong();
        final long leastSignificantBits = inputStream.readLong();
        final UUID uuid = new UUID(mostSignificantBits, leastSignificantBits);
        final OnlineUser user = plugin.findPlayer(uuid).orElse(null);
        if (user == null) {
            return;
        }

        try {
            final short messageLength = inputStream.readShort();
            final byte[] messageBody = new byte[messageLength];
            inputStream.readFully(messageBody);
            user.handleMessage(plugin, Request.deserialize(messageBody), channel.equals(getComponentChannel(isRequest)));
        } catch (InvalidClassException e) {
                plugin.log(Level.SEVERE, "Failed to deserialize request. Is the plugin up-to-date?");
        } catch (IOException | ClassNotFoundException e) {
            plugin.log(Level.SEVERE, "Failed to fully read plugin message", e);
        }
    }

    CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser requester, @NotNull UUID formatFor, boolean wantsJson, long requestTimeout);

    @Deprecated(since = "1.6", forRemoval = true)
    default CompletableFuture<List<String>> findServers(long requestTimeout) {
        return getServers(requestTimeout).thenApply(ArrayList::new);
    }

    CompletableFuture<Set<String>> getServers(long requestTimeout);

    void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions);

    File getDataFolder();

    default void loadConfig() {
        final Path settingsFile = getDataFolder().toPath().resolve("settings.yml");
        final Settings settings = YamlConfigurations.update(settingsFile, Settings.class);
        setSettings(settings);
    }

    void setSettings(Settings settings);

    void loadMessenger();

    Messenger getMessenger();

    Settings getSettings();
}
