package net.william278.papiproxybridge.messenger.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.config.Settings;
import net.william278.papiproxybridge.messenger.Messenger;
import org.checkerframework.checker.units.qual.K;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RedisMessenger extends Messenger {

    private final PAPIProxyBridge plugin;
    private RedisClient client;
    private StatefulRedisConnection<String, byte[]> connection;
    private final Settings.RedisSettings redisSettings;

    public RedisMessenger(@NotNull PAPIProxyBridge plugin, @NotNull Settings.RedisSettings redisSettings) {
        this.plugin = plugin;
        this.redisSettings = redisSettings;
    }

    @Override
    public void onEnable() {
        client = RedisClient.create(RedisURI.builder()
                .withHost(redisSettings.host())
                .withPort(redisSettings.port())
                .withPassword(redisSettings.password() == null ? null : redisSettings.password().toCharArray())
                .build());

        connection = client.connect(StringByteArrayCodec.INSTANCE);
        listen();
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
                plugin.handleMessage(plugin, string, bytes);
            }
        });

        pubSubConnection.async().subscribe(plugin.getChannel(), plugin.getComponentChannel());
    }
}
