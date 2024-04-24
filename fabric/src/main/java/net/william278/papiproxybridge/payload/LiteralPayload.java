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

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class LiteralPayload extends TemplatePayload {

    public static final Id<LiteralPayload> ID = new Id<>(new Identifier("papiproxybridge", "format"));
    public static final PacketCodec<RegistryByteBuf, LiteralPayload> CODEC = PacketCodec.of((value, buf) -> writeBytes(buf, value.bytes), LiteralPayload::new);

    public LiteralPayload(byte[] bytes) {
        super(bytes);
    }

    private LiteralPayload(PacketByteBuf buf) {
        this(getWrittenBytes(buf));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static String getChannel() {
        return "papiproxybridge:format";
    }

}