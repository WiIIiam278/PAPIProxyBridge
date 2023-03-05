package net.william278.papiproxybridge.user;

import com.velocitypowered.api.proxy.Player;
import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.VelocityPAPIProxyBridge;
import org.jetbrains.annotations.NotNull;

public class VelocityUser implements ProxyUser {

    private final Player player;

    private VelocityUser(@NotNull Player player) {
        this.player = player;
    }

    @NotNull
    public static VelocityUser adapt(@NotNull Player player) {
        return new VelocityUser(player);
    }

    @Override
    @NotNull
    public String getUsername() {
        return player.getUsername();
    }

    @Override
    public void sendPluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message) {
        player.sendPluginMessage(((VelocityPAPIProxyBridge) plugin).getBungeeChannel(), message);
    }
}
