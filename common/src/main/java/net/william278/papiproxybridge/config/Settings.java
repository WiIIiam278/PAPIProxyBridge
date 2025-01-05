package net.william278.papiproxybridge.config;

import de.exlll.configlib.Configuration;

@Configuration
@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class Settings {


    public enum Messenger {
        PLUGIN_MESSAGE,
        REDIS
    }

    public record RedisSettings(String host, int port, String password) {
    }

    private Messenger messenger = Messenger.PLUGIN_MESSAGE;
    private RedisSettings redis = new RedisSettings("localhost", 6379, null);

    public Messenger getMessenger() {
        return messenger;
    }

    public RedisSettings getRedis() {
        return redis;
    }


}
