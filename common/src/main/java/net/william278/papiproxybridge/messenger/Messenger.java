package net.william278.papiproxybridge.messenger;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class Messenger {

    public void onEnable() {

    }

    public void onDisable() {

    }

    public abstract void sendMessage(@NotNull UUID uuid, @NotNull String channel, byte @NotNull [] message);

}
