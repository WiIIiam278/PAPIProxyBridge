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

package net.william278.papiproxybridge.user;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import net.william278.papiproxybridge.PAPIProxyBridge;
import net.william278.papiproxybridge.VelocityPAPIProxyBridge;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;

public class VelocityUser implements ProxyUser {

    private final Player player;
    private boolean justSwitchedServer;

    private VelocityUser(@NotNull Player player) {
        this.player = player;
    }

    @NotNull
    public static VelocityUser adapt(@NotNull Player player) {
        return new VelocityUser(player);
    }

    @Override
    @NotNull
    public String getUsername() {
        return player.getUsername();
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public void sendPluginMessage(@NotNull PAPIProxyBridge plugin, @NotNull String channel, byte[] message) {
        player.getCurrentServer().ifPresent(server -> {
            if (!server.sendPluginMessage(new LegacyChannelIdentifier(channel), message)) {
                plugin.log(Level.SEVERE, "Failed to send plugin message to " + server.getServerInfo().getName()
                                         + " for player " + player.getUsername() + " on channel "
                                         + channel);
            }
        });
    }

    @Override
    @NotNull
    public String getServerName() {
        return player.getCurrentServer()
                .map(serverConnection -> serverConnection.getServerInfo().getName())
                .orElse("unknown");
    }

    @Override
    public boolean justSwitchedServer() {
        return justSwitchedServer;
    }

    public void setJustSwitchedServer(boolean justSwitchedServer) {
        this.justSwitchedServer = justSwitchedServer;
    }
}
