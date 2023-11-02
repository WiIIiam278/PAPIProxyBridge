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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.william278.papiproxybridge.FabricPAPIProxyBridge;
import net.william278.papiproxybridge.PAPIProxyBridge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class FabricUser implements OnlineUser {

    private final ServerPlayerEntity player;

    private FabricUser(@NotNull ServerPlayerEntity player) {
        this.player = player;
    }

    @NotNull
    public static FabricUser adapt(@NotNull ServerPlayerEntity player) {
        return new FabricUser(player);
    }

    @Override
    @NotNull
    public String getUsername() {
        return player.getName().getString();
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
        return player.getUuid();
    }

    @Override
    public void sendPluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message) {
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(new CustomPayload() {
            @Override
            public void write(PacketByteBuf buf) {
                buf.writeBytes(message);
            }

            @Override
            public Identifier id() {
                return new Identifier(channel);
            }
        });
        player.networkHandler.sendPacket(packet);
    }

    private Component getComponent(Text text) {
        return GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(text));
    }

    private Component translateKeys(TranslatableComponent translatable) {
        final String key = translatable.key();
        final @Nullable String translated = Objects.requireNonNullElse(
                Language.getInstance().get(key, translatable.fallback()),
                key
        );
        return translatable.fallback(translated);
    }

    @Override
    public void handlePluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message, boolean wantsJson) {
        FabricPAPIProxyBridge bridge = (FabricPAPIProxyBridge) plugin;
        Text formatted = bridge.formatPlaceholders(message.getFormatFor(), this, message.getMessage());
        Component original = getComponent(formatted);
        Component transformed = original.children().stream().map(component -> {
            if (component instanceof TranslatableComponent trans) {
                return translateKeys(trans);
            }
            return component;
        }).collect(Component.toComponent()).mergeStyle(original);
        String response = wantsJson ? GsonComponentSerializer.gson().serialize(transformed) : formatted.getString();
        message.setMessage(response);
        this.sendPluginMessage(plugin, message, wantsJson);
    }

    @NotNull
    public PlayerEntity getPlayer() {
        return player;
    }
}
