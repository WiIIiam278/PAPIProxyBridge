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

package net.william278.papiproxybridge.messenger.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.config.Settings;
import net.william278.papiproxybridge.messenger.Messenger;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;

public class RedisMessenger extends Messenger {

    private static final String CLIENT_NAME = "PAPIProxyBridge";

    private final PAPIProxyBridge plugin;
    private final boolean isRequest;
    private final Settings.RedisSettings redisSettings;
    private RedisClient client;
    private StatefulRedisConnection<String, byte[]> connection;

    /**
     * Create a new instance of the RedisMessenger
     *
     * @param plugin        the plugin instance
     * @param redisSettings the redis settings
     * @param isRequest     whether the messenger is for requests (backends) or responses (proxy)
     */
    public RedisMessenger(@NotNull PAPIProxyBridge plugin, @NotNull Settings.RedisSettings redisSettings, boolean isRequest) {
        this.plugin = plugin;
        this.redisSettings = redisSettings;
        this.isRequest = isRequest;
    }

    @Override
    public void onEnable() {
        try {
            createClient();
        } catch (Throwable e) {
            plugin.log(Level.SEVERE, "Failed to establish connection with Redis. "
                    + "Please check the supplied credentials in the config file", e);
            return;
        }

        connection = client.connect(StringByteArrayCodec.INSTANCE);
        listen();
    }

    private void createClient() {
        final Settings.RedisSettings.RedisCredentials credentials = redisSettings.getCredentials();
        final Settings.RedisSettings.RedisSentinel sentinel = redisSettings.getSentinel();

        if (sentinel.getNodes().isEmpty()) {
            client = RedisClient.create(RedisURI.builder()
                    .withHost(credentials.getHost())
                    .withPort(credentials.getPort())
                    .withPassword(credentials.getPassword() == null ? null : credentials.getPassword().toCharArray())
                    .withClientName(CLIENT_NAME)
                    .withSsl(credentials.isUseSsl())
                    .build());
            return;
        }

        plugin.log(Level.INFO, "Connecting with redis sentinel");
        final RedisURI.Builder builder = RedisURI.builder()
                .withSentinelMasterId(sentinel.getMaster());

        sentinel.getNodes().forEach(node -> {
            final String[] split = node.split(":");
            if (split.length != 2) {
                throw new IllegalArgumentException("Invalid sentinel node: " + node);
            }
            builder.withSentinel(split[0], Integer.parseInt(split[1]));
        });

        builder.withClientName(CLIENT_NAME)
                .withSsl(credentials.isUseSsl());
        client = RedisClient.create(builder.build());
    }

    @Override
    public void onDisable() {
        try {
            client.close();
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void sendMessage(@NotNull UUID uuid, @NotNull String channel, byte @NotNull [] message) {
        connection.async().publish(channel, message);
    }

    public void listen() {
        final StatefulRedisPubSubConnection<String, byte[]> pubSubConnection = client.connectPubSub(StringByteArrayCodec.INSTANCE);
        pubSubConnection.addListener(new RedisPubSubListener() {
            @Override
            public void message(String string, byte[] bytes) {
                plugin.handleMessage(plugin, string, bytes, isRequest);
            }
        });

        pubSubConnection.async().subscribe(PAPIProxyBridge.getChannel(isRequest), PAPIProxyBridge.getComponentChannel(isRequest));
    }
}
