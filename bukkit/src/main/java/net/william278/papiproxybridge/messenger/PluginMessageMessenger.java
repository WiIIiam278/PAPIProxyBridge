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

import lombok.RequiredArgsConstructor;
import net.william278.papiproxybridge.BukkitPAPIProxyBridge;
import net.william278.papiproxybridge.PAPIProxyBridge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public class PluginMessageMessenger extends Messenger implements PluginMessageListener {

    private final BukkitPAPIProxyBridge plugin;

    @Override
    public void onEnable() {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, PAPIProxyBridge.getChannel(false));
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, PAPIProxyBridge.getComponentChannel(false));
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, PAPIProxyBridge.getChannel(true), this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, PAPIProxyBridge.getComponentChannel(true), this);
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
        plugin.getExecutorService().submit(() -> plugin.handleMessage(plugin, channel, message, true));
    }
}
