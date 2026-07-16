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
import net.william278.papiproxybridge.user.Request;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PAPIProxyBridgeTest {

    @Test
    void ignoresMalformedFrames() {
        final TestBridge bridge = new TestBridge();
        assertDoesNotThrow(() -> bridge.handleMessage(bridge, PAPIProxyBridge.getChannel(true), new byte[0], true));
    }

    @Test
    void readsUnsignedPayloadLengths() throws Exception {
        final TestBridge bridge = new TestBridge();
        final String text = "x".repeat(32_720);
        final byte[] request = new Request(text, bridge.user.getUniqueId()).serialize();
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final DataOutputStream output = new DataOutputStream(bytes);
        final UUID userId = bridge.user.getUniqueId();
        output.writeLong(userId.getMostSignificantBits());
        output.writeLong(userId.getLeastSignificantBits());
        output.writeShort(request.length);
        output.write(request);

        assertDoesNotThrow(() -> bridge.handleMessage(bridge, PAPIProxyBridge.getChannel(true), bytes.toByteArray(), true));
        assertEquals(text, bridge.user.message.getMessage());
    }

    private static final class TestBridge implements PAPIProxyBridge {
        private final TestUser user = new TestUser(UUID.randomUUID());

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
            return CompletableFuture.completedFuture(text);
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

    private static final class TestUser implements OnlineUser {
        private final UUID uuid;
        private Request message;

        private TestUser(UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public @NotNull String getUsername() {
            return "user";
        }

        @Override
        public @NotNull UUID getUniqueId() {
            return uuid;
        }

        @Override
        public void handleMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message, boolean wantsJson) {
            this.message = message;
        }
    }
}
