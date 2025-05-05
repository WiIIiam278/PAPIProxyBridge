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

package net.william278.papiproxybridge.messenger;

import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.william278.papiproxybridge.FabricPAPIProxyBridge;
import net.william278.papiproxybridge.payload.ComponentPayload;
import net.william278.papiproxybridge.payload.LiteralPayload;
import net.william278.papiproxybridge.payload.TemplatePayload;
import net.william278.papiproxybridge.user.FabricUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class PluginMessageMessenger extends Messenger {

    private final FabricPAPIProxyBridge plugin;

    @Override
    public void onEnable() {
        ServerPlayNetworking.registerGlobalReceiver(LiteralPayload.REQUEST_ID,
                (minecraftServer, serverPlayerEntity,
                 serverPlayNetworkHandler, packetByteBuf,
                 packetSender) -> plugin.handleMessage(plugin, LiteralPayload.REQUEST_ID.toString(), TemplatePayload.getWrittenBytes(packetByteBuf), true));

        ServerPlayNetworking.registerGlobalReceiver(ComponentPayload.REQUEST_ID,
                (minecraftServer, serverPlayerEntity,
                 serverPlayNetworkHandler, packetByteBuf,
                 packetSender) -> plugin.handleMessage(plugin, ComponentPayload.REQUEST_ID.toString(), TemplatePayload.getWrittenBytes(packetByteBuf), true));
    }

    @Override
    public void sendMessage(@NotNull UUID uuid, @NotNull String channel, byte @NotNull [] message) {
        final Optional<FabricUser> optionalFabricUser = plugin.findPlayer(uuid);
        if (optionalFabricUser.isEmpty()) {
            return;
        }

        final FabricUser user = optionalFabricUser.get();
        final TemplatePayload templatePayload = channel.equals(ComponentPayload.REQUEST_ID.toString()) ?
                new ComponentPayload(message, false) :
                new LiteralPayload(message, false);

        final Packet<?> packet = new CustomPayloadS2CPacket(templatePayload.getPacketByteBuf());
        user.player().networkHandler.sendPacket(packet);
    }
}