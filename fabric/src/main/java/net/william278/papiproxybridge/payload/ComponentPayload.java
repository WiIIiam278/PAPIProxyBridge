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

package net.william278.papiproxybridge.payload;

//#if MC>=260102
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
//#else
//$$ import net.minecraft.network.PacketByteBuf;
//$$ import net.minecraft.network.codec.PacketCodec;
//$$ import net.minecraft.network.packet.CustomPayload;
//$$ import net.minecraft.util.Identifier;
//#endif
import net.william278.papiproxybridge.PAPIProxyBridge;

public final class ComponentPayload extends TemplatePayload {

//#if MC>=260102
    public static final CustomPacketPayload.Type<ComponentPayload> REQUEST_ID = new CustomPacketPayload.Type<>(Identifier.parse(PAPIProxyBridge.getComponentChannel(true)));
    public static final CustomPacketPayload.Type<ComponentPayload> RESPONSE_ID = new CustomPacketPayload.Type<>(Identifier.parse(PAPIProxyBridge.getComponentChannel(false)));
    public static final StreamCodec<FriendlyByteBuf, ComponentPayload> CODEC = StreamCodec.of((buf, value) -> writeBytes(buf, value.bytes), ComponentPayload::new);
//#else
//$$ public static final CustomPayload.Id<ComponentPayload> REQUEST_ID = new CustomPayload.Id<>(Identifier.of(PAPIProxyBridge.getComponentChannel(true)));
//$$ public static final CustomPayload.Id<ComponentPayload> RESPONSE_ID = new CustomPayload.Id<>(Identifier.of(PAPIProxyBridge.getComponentChannel(false)));
//$$ public static final PacketCodec<PacketByteBuf, ComponentPayload> CODEC = PacketCodec.of((value, buf) -> writeBytes(buf, value.bytes), ComponentPayload::new);
//#endif

    private final boolean isRequest;

    public ComponentPayload(byte[] bytes, boolean isRequest) {
        super(bytes);
        this.isRequest = isRequest;
    }

//#if MC>=260102
    private ComponentPayload(FriendlyByteBuf buf) {
//#else
//$$ private ComponentPayload(PacketByteBuf buf) {
//#endif
        this(getWrittenBytes(buf), true);
    }

    @Override
//#if MC>=260102
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
//#else
//$$ public Id<? extends CustomPayload> getId() {
//#endif
        return isRequest ? REQUEST_ID : RESPONSE_ID;
    }
}
