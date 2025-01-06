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

import me.clip.placeholderapi.libs.kyori.adventure.text.Component;
import me.clip.placeholderapi.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.william278.papiproxybridge.BukkitPAPIProxyBridge;
import net.william278.papiproxybridge.PAPIProxyBridge;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class BukkitUser implements OnlineUser {

    private final Player player;
    private boolean justSwitchedServer;

    private BukkitUser(@NotNull Player player) {
        this.player = player;
    }

    @NotNull
    public static BukkitUser adapt(@NotNull Player player) {
        return new BukkitUser(player);
    }

    @Override
    @NotNull
    public String getUsername() {
        return player.getName();
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public void handleMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message, boolean wantsJson) {
        ((BukkitPAPIProxyBridge) plugin).formatPlaceholders(message.getFormatFor(), this, message.getMessage()).thenAccept(formatted -> {
            message.setMessage(wantsJson ? GsonComponentSerializer.gson().serialize(Component.text(formatted)) : formatted);
            this.sendMessage(plugin, message, wantsJson, false);
        });
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean justSwitchedServer() {
        return justSwitchedServer;
    }

    public void setJustSwitchedServer(boolean justSwitchedServer) {
        this.justSwitchedServer = justSwitchedServer;
    }

}
