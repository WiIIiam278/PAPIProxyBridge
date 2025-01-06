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

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.william278.papiproxybridge.FabricPAPIProxyBridge;
import net.william278.papiproxybridge.payload.ComponentPayload;
import net.william278.papiproxybridge.payload.LiteralPayload;
import net.william278.papiproxybridge.user.FabricUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PluginMessageMessenger extends Messenger {

    private final FabricPAPIProxyBridge plugin;

    public PluginMessageMessenger(@NotNull FabricPAPIProxyBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        PayloadTypeRegistry.playC2S().register(LiteralPayload.ID, LiteralPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LiteralPayload.ID, LiteralPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ComponentPayload.ID, ComponentPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ComponentPayload.ID, ComponentPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(LiteralPayload.ID, (payload, context) -> plugin.handleMessage(plugin, LiteralPayload.getChannel(), payload.getBytes(), true));
        ServerPlayNetworking.registerGlobalReceiver(ComponentPayload.ID, (payload, context) -> plugin.handleMessage(plugin, ComponentPayload.getChannel(), payload.getBytes(), true));
    }

    @Override
    public void sendMessage(@NotNull UUID uuid, @NotNull String channel, byte @NotNull [] message) {
        final Optional<FabricUser> optionalFabricUser = plugin.findPlayer(uuid);
        if (optionalFabricUser.isEmpty()) {
            return;
        }
        final FabricUser user = optionalFabricUser.get();

        final CustomPayload payload = channel.equals(ComponentPayload.getChannel()) ?
                new ComponentPayload(message) :
                new LiteralPayload(message);
        final Packet<?> packet = new CustomPayloadS2CPacket(payload);
        user.getPlayer().networkHandler.sendPacket(packet);
    }

}
