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
import net.william278.papiproxybridge.user.ProxyUser;
import net.william278.papiproxybridge.user.Request;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public interface ProxyPAPIProxyBridge extends PAPIProxyBridge {


    @NotNull
    ConcurrentMap<UUID, CompletableFuture<String>> getRequests();

    default CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser requester, @NotNull UUID formatFor,
                                                    boolean wantsJson, long requestTimeout) {
        final Request request = new Request(text, formatFor);
        final CompletableFuture<String> future = new CompletableFuture<>();
        getRequests().putIfAbsent(request.getUuid(), future);
        future.orTimeout(requester.justSwitchedServer() ? requestTimeout * 2L : requestTimeout, TimeUnit.MILLISECONDS).exceptionally(throwable -> {
            getRequests().remove(request.getUuid());
            return text;
        });
        requester.sendPluginMessage(this, request, wantsJson);
        return future;
    }

    default CompletableFuture<List<String>> findServers(long requestTimeout) {
        final CompletableFuture<List<String>> future = new CompletableFuture<>();
        final Map<String, CompletableFuture<Boolean>> serverMap = getOnlineUsers().stream()
                .filter(user -> user instanceof ProxyUser)
                .map(user -> (ProxyUser) user)
                .collect(Collectors.toConcurrentMap(
                        ProxyUser::getServerName,
                        user -> createRequest(HANDSHAKE_PLACEHOLDER, user, user.getUniqueId(), false, requestTimeout)
                                .thenApply(message -> message.equals(HANDSHAKE_RESPONSE))
                ));

        CompletableFuture.allOf(serverMap.values().toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    final Set<String> servers = serverMap.entrySet().stream()
                            .filter(entry -> entry.getValue().getNow(false))
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toSet());
                    future.complete(List.copyOf(servers));
                });

        return future;
    }

}
