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

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class Request {
    private final UUID uuid;
    private final UUID formatFor;
    private String message;

    public Request(@NotNull String message, @NotNull UUID formatFor) {
        this.uuid = UUID.randomUUID();
        this.formatFor = formatFor;
        this.message = message;
    }

    private Request(@NotNull UUID uuid, @NotNull UUID formatFor, @NotNull String message) {
        this.uuid = uuid;
        this.formatFor = formatFor;
        this.message = message;
    }

    @Override
    public String toString() {
        return uuid.toString() + message;
    }

    @NotNull
    public static Request fromString(@NotNull String string) {
        return new Request(
                UUID.fromString(string.substring(0, 36)),
                UUID.fromString(string.substring(36, 72)),
                string.substring(72)
        );
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public UUID getFormatFor() {
        return formatFor;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    public void setMessage(@NotNull String message) {
        this.message = message;
    }

}
