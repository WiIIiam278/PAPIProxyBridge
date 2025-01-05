package net.william278.papiproxybridge.messenger;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.william278.papiproxybridge.FabricPAPIProxyBridge;
import net.william278.papiproxybridge.payload.ComponentPayload;
import net.william278.papiproxybridge.payload.LiteralPayload;
import net.william278.papiproxybridge.user.FabricUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PluginMessageMessenger extends Messenger {

    private final FabricPAPIProxyBridge plugin;

    public PluginMessageMessenger(@NotNull FabricPAPIProxyBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        PayloadTypeRegistry.playC2S().register(LiteralPayload.ID, LiteralPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LiteralPayload.ID, LiteralPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ComponentPayload.ID, ComponentPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ComponentPayload.ID, ComponentPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(LiteralPayload.ID, (payload, context) -> plugin.handleMessage(plugin, LiteralPayload.getChannel(), payload.getBytes()));
        ServerPlayNetworking.registerGlobalReceiver(ComponentPayload.ID, (payload, context) -> plugin.handleMessage(plugin, ComponentPayload.getChannel(), payload.getBytes()));
    }

    @Override
    public void sendMessage(@NotNull UUID uuid, @NotNull String channel, byte @NotNull [] message) {
        final Optional<FabricUser> optionalFabricUser = plugin.findPlayer(uuid);
        if (optionalFabricUser.isEmpty()) {
            return;
        }
        final FabricUser user = optionalFabricUser.get();

        final CustomPayload payload = channel.equals(ComponentPayload.getChannel()) ?
                new ComponentPayload(message) :
                new LiteralPayload(message);
        final Packet<?> packet = new CustomPayloadS2CPacket(payload);
        user.getPlayer().networkHandler.sendPacket(packet);
    }

}
