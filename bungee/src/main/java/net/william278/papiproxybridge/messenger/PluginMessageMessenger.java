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

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.william278.papiproxybridge.BungeePAPIProxyBridge;
import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.user.BungeeUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PluginMessageMessenger extends Messenger implements Listener {

    private final BungeePAPIProxyBridge plugin;

    public PluginMessageMessenger(@NotNull BungeePAPIProxyBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        // Register the plugin message channel
        plugin.getProxy().registerChannel(PAPIProxyBridge.getChannel(true));
        plugin.getProxy().registerChannel(PAPIProxyBridge.getComponentChannel(true));
        plugin.getProxy().registerChannel(PAPIProxyBridge.getChannel(false));
        plugin.getProxy().registerChannel(PAPIProxyBridge.getComponentChannel(false));

        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @Override
    public void sendMessage(@NotNull UUID uuid, @NotNull String channel, byte @NotNull [] message) {
        final Optional<BungeeUser> optionalBungeeUser = plugin.findPlayer(uuid);
        if (optionalBungeeUser.isEmpty()) {
            return;
        }

        final BungeeUser user = optionalBungeeUser.get();
        final ProxiedPlayer player = user.player();
        player.getServer().getInfo().sendData(channel, message);
    }

    @EventHandler
    public void onPluginMessageReceived(net.md_5.bungee.api.event.PluginMessageEvent event) {
        plugin.handleMessage(plugin, event.getTag(), event.getData(), false);
    }

    @Override
    public void onDisable() {
        plugin.getProxy().unregisterChannel(PAPIProxyBridge.getChannel(true));
        plugin.getProxy().unregisterChannel(PAPIProxyBridge.getComponentChannel(true));
        plugin.getProxy().unregisterChannel(PAPIProxyBridge.getChannel(false));
        plugin.getProxy().unregisterChannel(PAPIProxyBridge.getComponentChannel(false));
    }
}
