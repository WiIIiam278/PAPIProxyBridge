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

import net.william278.papiproxybridge.config.Settings;
import net.william278.papiproxybridge.messenger.Messenger;
import net.william278.papiproxybridge.user.OnlineUser;
import net.william278.papiproxybridge.user.ProxyUser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyPAPIProxyBridgeTest {

    @Test
    void serverLookupHonorsRequestTimeout() throws Exception {
        final TestBridge bridge = new TestBridge();

        assertEquals(Set.of(), bridge.getServers(10).get(1, TimeUnit.SECONDS));
        assertTrue(bridge.requests.isEmpty());
    }

    private static final class TestBridge implements ProxyPAPIProxyBridge {
        private final ConcurrentMap<UUID, CompletableFuture<String>> requests = new ConcurrentHashMap<>();
        private final TestUser user = new TestUser();
        private final Settings settings = new Settings();
        private final Messenger messenger = new Messenger() {
            @Override
            public void sendMessage(@NotNull UUID uuid, @NotNull String channel, byte @NotNull [] message) {
            }
        };

        @Override
        public @NotNull ConcurrentMap<UUID, CompletableFuture<String>> getRequests() {
            return requests;
        }

        @Override
        public String getServerType() {
            return "test";
        }

        @Override
        public @NotNull Collection<? extends OnlineUser> getOnlineUsers() {
            return List.of(user);
        }

        @Override
        public Optional<? extends OnlineUser> findPlayer(@NotNull UUID uuid) {
            return user.getUniqueId().equals(uuid) ? Optional.of(user) : Optional.empty();
        }

        @Override
        public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        }

        @Override
        public File getDataFolder() {
            return new File(".");
        }

        @Override
        public void setSettings(Settings settings) {
        }

        @Override
        public void loadMessenger() {
        }

        @Override
        public Messenger getMessenger() {
            return messenger;
        }

        @Override
        public Settings getSettings() {
            return settings;
        }
    }

    private static final class TestUser implements ProxyUser {
        private final UUID uuid = UUID.randomUUID();

        @Override
        public @NotNull String getUsername() {
            return "user";
        }

        @Override
        public @NotNull UUID getUniqueId() {
            return uuid;
        }

        @Override
        public @NotNull String getServerName() {
            return "backend";
        }
    }
}
