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

import com.google.common.collect.Lists;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.events.CustomPayloadCallback;
import net.william278.papiproxybridge.user.FabricUser;
import net.william278.papiproxybridge.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class FabricPAPIProxyBridge implements DedicatedServerModInitializer, PAPIProxyBridge {
    public static final Logger LOGGER = LoggerFactory.getLogger("FabricPAPIProxyBridge");
    private static MinecraftServer server;
    private List<FabricUser> fabricUsers;

    @Override
    public void onInitializeServer() {
        PlaceholderAPI.register(this);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> FabricPAPIProxyBridge.server = server);
        fabricUsers = Lists.newArrayList();

        CustomPayloadCallback.EVENT.register((channel, byteBuf) -> {
            if (channel.equals(getChannel()) || channel.equals(getComponentChannel())) {
                this.handlePluginMessage(this, channel, byteBuf.array());
            }
        });

        handleEvents();
    }

    private void handleEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            FabricUser user = FabricUser.adapt(handler.player);
            fabricUsers.add(user);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            fabricUsers.removeIf(user -> user.getUniqueId().equals(handler.player.getUuid()));
        });
    }

    @Override
    @NotNull
    public List<? extends OnlineUser> getOnlineUsers() {
        return fabricUsers;
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull UUID uuid) {
        return fabricUsers.stream().filter(user -> user.getUniqueId().equals(uuid)).map(u -> (OnlineUser) u).findFirst();
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull String username) {
        return fabricUsers.stream().filter(user -> user.getUsername().equals(username)).map(u -> (OnlineUser) u).findFirst();
    }

    @Override
    public CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser requester, @NotNull UUID formatFor, boolean wantsJson, long requestTimeout) {
        String json = formatPlaceholders(formatFor, (FabricUser) requester, text).getString();
        return CompletableFuture.completedFuture(json);
    }

    @Override
    public CompletableFuture<List<String>> findServers(long requestTimeout) {
        throw new UnsupportedOperationException("Cannot fetch the list of servers from a backend Fabric server.");
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        if (exceptions.length > 0) {
            LOGGER.error(message, exceptions[0]);
        } else {
            LOGGER.info(message);
        }
    }

    @NotNull
    public final Text formatPlaceholders(@NotNull UUID formatFor, @NotNull FabricUser requester, @NotNull String text) {
        text = text.replaceAll(HANDSHAKE_PLACEHOLDER, HANDSHAKE_RESPONSE);
        return Placeholders.parseText(Text.of(text), PlaceholderContext.of(
                ((FabricUser) findPlayer(formatFor).orElse(requester)).getPlayer())
        );
    }
}
