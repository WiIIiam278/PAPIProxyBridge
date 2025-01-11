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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.UUID;

@AllArgsConstructor
@Getter
public final class Request {

    private static final short VERSION = 1;

    private final UUID uuid;
    private final UUID formatFor;
    @Setter
    private String message;

    public Request(@NotNull String message, @NotNull UUID formatFor) {
        this.uuid = UUID.randomUUID();
        this.formatFor = formatFor;
        this.message = message;
    }

    @Override
    public String toString() {
        return uuid.toString() + formatFor.toString() + message;
    }

    public byte @NotNull [] serialize() throws IOException {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        final DataOutputStream dataStream = new DataOutputStream(byteStream);

        dataStream.writeShort(VERSION);
        dataStream.writeLong(uuid.getMostSignificantBits());
        dataStream.writeLong(uuid.getLeastSignificantBits());
        dataStream.writeLong(formatFor.getMostSignificantBits());
        dataStream.writeLong(formatFor.getLeastSignificantBits());
        dataStream.writeUTF(message);

        return byteStream.toByteArray();
    }

    @NotNull
    public static Request deserialize(byte @NotNull [] data) throws IOException, ClassNotFoundException {
        final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        final DataInputStream dataStream = new DataInputStream(byteStream);

        final short version = dataStream.readShort();
        if (version != VERSION) {
            throw new IllegalStateException("Invalid version: " + version + ". Make sure you are using the latest version of PapiProxyBridge on all servers.");
        }
        final UUID uuid = new UUID(dataStream.readLong(), dataStream.readLong());
        final UUID formatFor = new UUID(dataStream.readLong(), dataStream.readLong());
        final String message = dataStream.readUTF();

        return new Request(uuid, formatFor, message);
    }
}
