package net.william278.papiproxybridge.messenger;

import net.william278.papiproxybridge.BukkitPAPIProxyBridge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PluginMessageMessenger extends Messenger implements PluginMessageListener {

    private final BukkitPAPIProxyBridge plugin;

    public PluginMessageMessenger(@NotNull BukkitPAPIProxyBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, plugin.getChannel());
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, plugin.getComponentChannel());
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, plugin.getChannel(), this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, plugin.getComponentChannel(), this);
    }

    @Override
    public void onDisable() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
    }

    @Override
    public void sendMessage(@NotNull UUID uuid, @NotNull String channel, byte @NotNull [] message) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }

        player.sendPluginMessage(plugin, channel, message);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        plugin.handleMessage(plugin, channel, message);
    }
}
