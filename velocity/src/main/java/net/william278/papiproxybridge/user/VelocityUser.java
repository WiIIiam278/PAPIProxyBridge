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

import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record VelocityUser(@NotNull Player player) implements ProxyUser {

    @NotNull
    public static VelocityUser adapt(@NotNull Player player) {
        return new VelocityUser(player);
    }

    @Override
    @NotNull
    public String getUsername() {
        return player.getUsername();
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    @NotNull
    public String getServerName() {
        return player.getCurrentServer()
                .map(serverConnection -> serverConnection.getServerInfo().getName())
                .orElse("unknown");
    }

    @Override
    public boolean isConnected() {
        return player.isActive();
    }
}
