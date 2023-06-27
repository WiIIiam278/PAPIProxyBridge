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

package net.william278.papiproxybridge;

import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.user.BungeeUser;
import net.william278.papiproxybridge.user.OnlineUser;
import org.bstats.bungeecord.Metrics;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BungeePAPIProxyBridge extends Plugin implements ProxyPAPIProxyBridge, Listener {

    private final Map<UUID, CompletableFuture<String>> requests = new HashMap<>();

    @Override
    public void onEnable() {
        // Register the plugin message channel
        getProxy().registerChannel(getChannel());

        // Register the plugin message listener
        getProxy().getPluginManager().registerListener(this, this);

        // Register the plugin with the API
        PlaceholderAPI.register(this);

        // Metrics
        new Metrics(this, 17879);

        getLogger().info("PAPIProxyBridge (" + getProxy().getName() + ") has been enabled!");
    }

    @Override
    public void onDisable() {
        // Unregister the plugin message channel
        getProxy().unregisterChannel(getChannel());

        // Unregister the plugin message listener
        getProxy().getPluginManager().unregisterListener(this);
    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent event) {
        this.handlePluginMessage(this, event.getTag(), event.getData());
    }

    @Override
    @NotNull
    public Map<UUID, CompletableFuture<String>> getRequests() {
        return requests;
    }

    @Override
    @NotNull
    public List<BungeeUser> getOnlineUsers() {
        return getProxy().getPlayers().stream().map(BungeeUser::adapt).toList();
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(getProxy().getPlayer(uuid)).map(BungeeUser::adapt);
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull String username) {
        return Optional.ofNullable(getProxy().getPlayer(username)).map(BungeeUser::adapt);
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        if (exceptions.length > 0) {
            getLogger().log(level, message, exceptions[0]);
        } else {
            getLogger().log(level, message);
        }
    }

}
