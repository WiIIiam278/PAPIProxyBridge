package net.william278.papiproxybridge.user;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.william278.papiproxybridge.PAPIProxyBridge;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BungeeUser implements ProxyUser {

    private final ProxiedPlayer player;

    private BungeeUser(@NotNull ProxiedPlayer player) {
        this.player = player;
    }

    @NotNull
    public static BungeeUser adapt(@NotNull ProxiedPlayer player) {
        return new BungeeUser(player);
    }

    @Override
    @NotNull
    public String getUsername() {
        return player.getName();
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public void sendPluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message) {
        player.getServer().getInfo().sendData(channel, message);
    }
}
