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

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.william278.papiproxybridge.FabricPAPIProxyBridge;
import net.william278.papiproxybridge.PAPIProxyBridge;
import org.jetbrains.annotations.NotNull;

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
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBytes(message);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(new Identifier(channel), buf);
        player.networkHandler.sendPacket(packet);
    }

    @Override
    public void handlePluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message, boolean wantsGson) {
        FabricPAPIProxyBridge bridge = (FabricPAPIProxyBridge) plugin;
        Text formatted = bridge.formatPlaceholders(message.getFormatFor(), this, message.getMessage());
        String response = wantsGson ? Text.Serializer.toJson(formatted) : formatted.getString();
        message.setMessage(response);
        this.sendPluginMessage(plugin, message, wantsGson);
    }

    @NotNull
    public PlayerEntity getPlayer() {
        return player;
    }
}
