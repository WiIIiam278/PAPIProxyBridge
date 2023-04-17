package net.william278.papiproxybridge.user;

import com.velocitypowered.api.proxy.Player;
import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.VelocityPAPIProxyBridge;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;

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
    @NotNull
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public void sendPluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message) {
        player.getCurrentServer().ifPresent(server -> {
            if (!server.sendPluginMessage(((VelocityPAPIProxyBridge) plugin).getChannelIdentifier(), message)) {
                plugin.log(Level.SEVERE, "Failed to send plugin message to " + server.getServerInfo().getName()
                                         + " for player " + player.getUsername() + " on channel " + ((VelocityPAPIProxyBridge) plugin).getChannelIdentifier().getId());
            }
        });
    }
}
