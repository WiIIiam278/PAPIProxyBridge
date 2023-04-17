package net.william278.papiproxybridge.user;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.william278.papiproxybridge.FabricPAPIProxyBridge;
import net.william278.papiproxybridge.PAPIProxyBridge;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FabricUser implements OnlineUser {

    private final ServerPlayerEntity player;

    private FabricUser(@NotNull ServerPlayerEntity player) {
        this.player = player;
    }

    @NotNull
    public static FabricUser adapt(@NotNull ServerPlayerEntity player) {
        return new FabricUser(player);
    }

    @Override
    @NotNull
    public String getUsername() {
        return player.getName().getString();
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
        return player.getUuid();
    }

    @Override
    public void sendPluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBytes(message);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(new Identifier(channel), buf);
        player.networkHandler.sendPacket(packet);
    }

    @Override
    public void handlePluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message) {
        FabricPAPIProxyBridge bridge = (FabricPAPIProxyBridge) plugin;
        message.setMessage(bridge.formatPlaceholders(this, Text.of(message.getMessage())).getString());
        this.sendPluginMessage(plugin, message);
    }

    @NotNull
    public PlayerEntity getPlayer() {
        return player;
    }
}
