package net.william278.papiproxybridge.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.PacketByteBuf;

@FunctionalInterface
public interface CustomPayloadCallback {
    Event<CustomPayloadCallback> EVENT = EventFactory.createArrayBacked(CustomPayloadCallback.class,
            (listeners) -> (channel, byteBuf) -> {
                for (CustomPayloadCallback listener : listeners) {
                    // Invoke all event listeners with the provided player and death message.
                    listener.invoke(channel, byteBuf);
                }
            });

    void invoke(String channel, PacketByteBuf byteBuf);
}