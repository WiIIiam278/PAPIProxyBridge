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

package net.william278.papiproxybridge.api;

import net.kyori.adventure.text.Component;
import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.config.Settings;
import net.william278.papiproxybridge.messenger.Messenger;
import net.william278.papiproxybridge.user.OnlineUser;
import net.william278.papiproxybridge.user.Request;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceholderAPITest {

    private final TestUser requester = new TestUser(UUID.randomUUID());
    private TestBridge bridge;
    private PlaceholderAPI api;

    @BeforeEach
    void setUp() {
        bridge = new TestBridge(requester);
        PlaceholderAPI.register(bridge);
        api = PlaceholderAPI.createInstance();
        api.setRequestTimeout(100);
    }

    @Test
    void cachesByRequesterAndFormattingTarget() {
        final UUID target = UUID.randomUUID();
        bridge.response = "target";
        assertEquals("target", api.formatPlaceholders("%name%", requester, target).join());

        bridge.response = "cached";
        assertEquals("target", api.formatPlaceholders("%name%", requester, target).join());

        bridge.response = "requester";
        assertEquals("requester", api.formatPlaceholders("%name%", requester, requester.getUniqueId()).join());

        bridge.response = "other requester";
        assertEquals("other requester", api.formatPlaceholders("%name%", new TestUser(UUID.randomUUID()), target).join());
        assertEquals(3, bridge.requests.get());
    }

    @Test
    void componentCacheUsesRequesterAndFormattingTarget() {
        final UUID target = UUID.randomUUID();
        bridge.response = "{\"text\":\"target\"}";
        assertEquals(Component.text("target"), api.formatComponentPlaceholders("%name%", requester, target).join());

        bridge.response = "{\"text\":\"cached\"}";
        assertEquals(Component.text("target"), api.formatComponentPlaceholders("%name%", requester, target).join());

        bridge.response = "{\"text\":\"requester\"}";
        assertEquals(Component.text("requester"), api.formatComponentPlaceholders("%name%", requester, requester.getUniqueId()).join());

        bridge.response = "{\"text\":\"other requester\"}";
        assertEquals(Component.text("other requester"), api.formatComponentPlaceholders("%name%", new TestUser(UUID.randomUUID()), target).join());
        assertEquals(3, bridge.requests.get());
    }

    @Test
    void retriesFailedRequests() {
        api.setRetryTimes(1);
        bridge.failures = 1;
        bridge.response = "formatted";

        assertEquals("formatted", api.formatPlaceholders("%name%", requester).join());
        assertEquals(2, bridge.requests.get());
    }

    private static final class TestBridge implements PAPIProxyBridge {
        private final TestUser user;
        private final AtomicInteger requests = new AtomicInteger();
        private String response;
        private int failures;

        private TestBridge(TestUser user) {
            this.user = user;
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
        public CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser requester,
                                                       @NotNull UUID formatFor, boolean wantsJson, long requestTimeout) {
            requests.incrementAndGet();
            if (failures-- > 0) {
                return CompletableFuture.failedFuture(new IllegalStateException("failed"));
            }
            return CompletableFuture.completedFuture(response);
        }

        @Override
        public CompletableFuture<Set<String>> getServers(long requestTimeout) {
            return CompletableFuture.completedFuture(Set.of());
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
            return null;
        }

        @Override
        public Settings getSettings() {
            return new Settings();
        }
    }

    private record TestUser(UUID uuid) implements OnlineUser {
        @Override
        public @NotNull String getUsername() {
            return "requester";
        }

        @Override
        public @NotNull UUID getUniqueId() {
            return uuid;
        }

        @Override
        public void handleMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message, boolean wantsJson) {
        }
    }
}
