/*
 * This file is part of PAPIProxyBridge, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.papiproxybridge.messenger;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.VelocityPAPIProxyBridge;
import net.william278.papiproxybridge.user.VelocityUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class PluginMessageMessenger extends Messenger {

    private final VelocityPAPIProxyBridge plugin;
    private ChannelIdentifier responseChannelIdentifier;
    private ChannelIdentifier responseComponentChannelIdentifier;

    public PluginMessageMessenger(@NotNull VelocityPAPIProxyBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        responseChannelIdentifier = new LegacyChannelIdentifier(PAPIProxyBridge.getChannel(false));
        responseComponentChannelIdentifier = new LegacyChannelIdentifier(PAPIProxyBridge.getComponentChannel(false));
        plugin.getServer().getChannelRegistrar().register(this.responseChannelIdentifier);
        plugin.getServer().getChannelRegistrar().register(this.responseComponentChannelIdentifier);
        plugin.getServer().getEventManager().register(plugin, this);
    }

    @Override
    public void sendMessage(@NotNull UUID uuid, @NotNull String channel, byte @NotNull [] message) {
        final Optional<VelocityUser> optionalVelocityUser = plugin.findPlayer(uuid);
        if (optionalVelocityUser.isEmpty()) {
            return;
        }

        final VelocityUser user = optionalVelocityUser.get();
        final Player player = user.player();
        player.getCurrentServer().ifPresent(server -> {
            if (!server.sendPluginMessage(new LegacyChannelIdentifier(channel), message)) {
                plugin.log(Level.SEVERE, "Failed to send plugin message to " + server.getServerInfo().getName()
                        + " for player " + player.getUsername() + " on channel "
                        + channel);
            }
        });
    }

    @Subscribe
    public void onPluginMessageReceived(@NotNull PluginMessageEvent event) {
        final ChannelIdentifier channelId = event.getIdentifier();
        if (!channelId.equals(this.responseChannelIdentifier) && !channelId.equals(this.responseComponentChannelIdentifier)) {
            return;
        }

        plugin.handleMessage(plugin, event.getIdentifier().getId(), event.getData(), false);
        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }

    @Override
    public void onDisable() {
        plugin.getServer().getChannelRegistrar().unregister(this.responseChannelIdentifier);
        plugin.getServer().getChannelRegistrar().unregister(this.responseComponentChannelIdentifier);
        plugin.getServer().getEventManager().unregisterListener(plugin, this);
    }
}
