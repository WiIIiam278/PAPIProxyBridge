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

package net.william278.papiproxybridge.mixin;

import net.fabricmc.fabric.impl.networking.payload.RetainedPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.william278.papiproxybridge.events.CustomPayloadCallback;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin {
    @Shadow @Final private static Logger LOGGER;

    @Inject(at = @At("HEAD"), method = "onCustomPayload(Lnet/minecraft/network/packet/c2s/common/CustomPayloadC2SPacket;)V")
    private void papiProxyBridge$handlePluginMessage(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        try {
            if (!(packet.payload() instanceof RetainedPayload retainedPayload)) {
                System.out.println("Different: " + packet.payload().getClass().getName());
                return;
            }
            retainedPayload.buf().resetReaderIndex();
            retainedPayload.buf().skipBytes(retainedPayload.buf().readableBytes());
            retainedPayload.buf().release();
            System.out.println(retainedPayload.buf().copy());
            CustomPayloadCallback.EVENT.invoker().invoke(packet.payload().id().toString(), retainedPayload.buf());
        } catch (Exception e) {
            LOGGER.error("Exception handling plugin message", e);
        }
    }
}
