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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.user.FabricUser;
import net.william278.papiproxybridge.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class FabricPAPIProxyBridge implements DedicatedServerModInitializer, PAPIProxyBridge {

    public static final Logger LOGGER = LoggerFactory.getLogger("FabricPAPIProxyBridge");
    public static final Identifier FORMAT = new Identifier("papiproxybridge", "format");
    public static final Identifier COMPONENT = new Identifier("papiproxybridge", "component");
    private Map<UUID, FabricUser> fabricUsers;

    @Override
    public void onInitializeServer() {
        fabricUsers = Maps.newConcurrentMap();
        PlaceholderAPI.register(this);

        handleEvents();
        handlePackets();

        LOGGER.info(getLoadMessage());
    }

    private void handleEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            FabricUser user = FabricUser.adapt(handler.player);
            fabricUsers.put(user.getUniqueId(), user);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> fabricUsers.remove(handler.player.getUuid()));
    }

    private void handlePackets() {
        ServerPlayNetworking.registerGlobalReceiver(FORMAT, (server, player, handler, buf, responseSender) ->
                this.handlePluginMessage(this, FORMAT.toString(), getWrittenBytes(buf)));
        ServerPlayNetworking.registerGlobalReceiver(COMPONENT, (server, player, handler, buf, responseSender) ->
                this.handlePluginMessage(this, COMPONENT.toString(), getWrittenBytes(buf)));
    }

    @VisibleForTesting
    public byte[] getWrittenBytes(ByteBuf buf) {
        int i = buf.writerIndex();
        byte[] bs = new byte[i];
        buf.getBytes(0, bs);
        return bs;
    }

    @Override
    public String getServerType() {
        return "Fabric";
    }

    @Override
    @NotNull
    public Collection<? extends FabricUser> getOnlineUsers() {
        return fabricUsers.values();
    }

    @Override
    public Optional<FabricUser> findPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(fabricUsers.get(uuid));
    }

    @Override
    public Optional<FabricUser> findPlayer(@NotNull String username) {
        return fabricUsers.values()
                .stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser requester, @NotNull UUID formatFor, boolean wantsJson, long requestTimeout) {
        String json = formatPlaceholders(formatFor, (FabricUser) requester, text).getString();
        return CompletableFuture.completedFuture(json);
    }

    @Override
    public CompletableFuture<Set<String>> findServers(long requestTimeout) {
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
                findPlayer(formatFor).orElse(requester).getPlayer())
        );
    }
}
