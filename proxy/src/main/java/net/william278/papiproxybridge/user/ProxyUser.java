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

import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.ProxyPAPIProxyBridge;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

public interface ProxyUser extends OnlineUser {

    @Override
    default void handleMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message, boolean wantsJson) {
        final ConcurrentMap<UUID, CompletableFuture<String>> requests = ((ProxyPAPIProxyBridge) plugin).getRequests();
        CompletableFuture<String> future = requests.get(message.getUuid());

        if (future != null) {
            future.complete(message.getMessage());
            requests.remove(message.getUuid());
        }
    }

    @NotNull
    String getServerName();

}
