package net.william278.papiproxybridge.mixin;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.william278.papiproxybridge.events.CustomPayloadCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Inject(at = @At("HEAD"), method = "onCustomPayload(Lnet/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;)V")
    private void papiProxyBridge$handlePluginMessage(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        CustomPayloadCallback.EVENT.invoker().invoke(packet.getChannel().toString(), packet.getData());
    }
}
