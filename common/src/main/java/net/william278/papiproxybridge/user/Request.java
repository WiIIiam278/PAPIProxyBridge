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
public final class Request implements Serializable {

    // Use serialVersionUID for compatibility with multiple versions of the plugin
    @Serial
    private static final long serialVersionUID = 1L;

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
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(this);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }

    @NotNull
    public static Request deserialize(byte @NotNull [] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (Request) objectInputStream.readObject();
        }
    }
}
