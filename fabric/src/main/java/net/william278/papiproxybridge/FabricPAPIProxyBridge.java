package net.william278.papiproxybridge;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.events.CustomPayloadCallback;
import net.william278.papiproxybridge.user.FabricUser;
import net.william278.papiproxybridge.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class FabricPAPIProxyBridge implements ModInitializer, PAPIProxyBridge {
    public static final Logger LOGGER = LoggerFactory.getLogger("FabricPAPIProxyBridge");
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        PlaceholderAPI.register(this);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> FabricPAPIProxyBridge.server = server);

        CustomPayloadCallback.EVENT.register((channel, byteBuf) -> {
            if (channel.equals(getChannel())) {
                this.handlePluginMessage(this, channel, byteBuf.getWrittenBytes());
            }
        });
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(uuid)).map(FabricUser::adapt);
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull String username) {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(username)).map(FabricUser::adapt);
    }

    @Override
    public CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser user) {
        String json = formatPlaceholders((FabricUser) user, Text.of(text)).getString();
        return CompletableFuture.completedFuture(json);
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        if (exceptions.length > 0) {
            LOGGER.error(message, exceptions[0]);
        } else {
            LOGGER.info(message);
        }
    }

    @NotNull
    public final Text formatPlaceholders(@NotNull FabricUser user, @NotNull Text text) {
        return Placeholders.parseText(text, PlaceholderContext.of(user.getPlayer()));
    }
}
