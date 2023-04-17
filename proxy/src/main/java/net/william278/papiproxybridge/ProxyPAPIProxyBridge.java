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

package net.william278.papiproxybridge;

import net.william278.papiproxybridge.user.OnlineUser;
import net.william278.papiproxybridge.user.Request;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface ProxyPAPIProxyBridge extends PAPIProxyBridge {

    @NotNull
    Map<UUID, CompletableFuture<String>> getRequests();

    default CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser requester, @NotNull UUID formatFor) {
        final Request request = new Request(text, formatFor);
        final CompletableFuture<String> future = new CompletableFuture<>();
        getRequests().put(request.getUuid(), future);
        future.orTimeout(800, TimeUnit.MILLISECONDS).exceptionally(throwable -> {
            getRequests().remove(request.getUuid());
            return text;
        });
        requester.sendPluginMessage(this, request);
        return future;
    }

}
