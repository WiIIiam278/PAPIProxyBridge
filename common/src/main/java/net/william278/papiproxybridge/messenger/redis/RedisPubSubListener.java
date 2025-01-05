package net.william278.papiproxybridge.messenger.redis;

public abstract class RedisPubSubListener implements io.lettuce.core.pubsub.RedisPubSubListener<String, byte[]> {

    @Override
    public void message(String string, String k1, byte[] bytes) {

    }

    @Override
    public void subscribed(String string, long l) {

    }

    @Override
    public void psubscribed(String string, long l) {

    }

    @Override
    public void unsubscribed(String string, long l) {

    }

    @Override
    public void punsubscribed(String string, long l) {

    }
}
