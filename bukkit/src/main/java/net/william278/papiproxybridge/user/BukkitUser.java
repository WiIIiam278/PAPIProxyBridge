package net.william278.papiproxybridge.user;

import net.william278.papiproxybridge.BukkitPAPIProxyBridge;
import net.william278.papiproxybridge.PAPIProxyBridge;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BukkitUser implements OnlineUser {

    private final Player player;

    private BukkitUser(@NotNull Player player) {
        this.player = player;
    }

    @NotNull
    public static BukkitUser adapt(@NotNull Player player) {
        return new BukkitUser(player);
    }

    @Override
    @NotNull
    public String getUsername() {
        return player.getName();
    }

    @Override
    public void sendPluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message) {
        player.sendPluginMessage((BukkitPAPIProxyBridge) plugin, channel, message);
    }

    @Override
    public void handlePluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message) {
        message.setMessage(((BukkitPAPIProxyBridge) plugin).formatPlaceholders(this, message.getMessage()));
        this.sendPluginMessage(plugin, message);
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

}
