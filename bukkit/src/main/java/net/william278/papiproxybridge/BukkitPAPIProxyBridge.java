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

import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.papi.Formatter;
import net.william278.papiproxybridge.user.BukkitUser;
import net.william278.papiproxybridge.user.OnlineUser;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BukkitPAPIProxyBridge extends JavaPlugin implements PAPIProxyBridge, PluginMessageListener {
    private Formatter formatter;

    @Override
    public void onLoad() {
        // Initialize the formatter
        formatter = new Formatter();
    }

    @Override
    public void onEnable() {
        // Register the plugin message channel
        getServer().getMessenger().registerOutgoingPluginChannel(this, getChannel());
        getServer().getMessenger().registerOutgoingPluginChannel(this, getComponentChannel());
        getServer().getMessenger().registerIncomingPluginChannel(this, getChannel(), this);
        getServer().getMessenger().registerIncomingPluginChannel(this, getComponentChannel(), this);

        // Register the plugin with the API
        PlaceholderAPI.register(this);

        // Metrics
        new Metrics(this, 17880);

        getLogger().info("PAPIProxyBridge (" + getServer().getName() + ") has been enabled!");
    }

    @Override
    public void onDisable() {
        // Unregister the plugin message channel
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    @Override
    @NotNull
    public List<BukkitUser> getOnlineUsers() {
        return getServer().getOnlinePlayers().stream().map(BukkitUser::adapt).toList();
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(getServer().getPlayer(uuid)).map(BukkitUser::adapt);
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull String username) {
        return Optional.ofNullable(getServer().getPlayerExact(username)).map(BukkitUser::adapt);
    }

    @Override
    public CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser requester, @NotNull UUID formatFor, boolean wantsJson) {
        return CompletableFuture.completedFuture(formatPlaceholders(formatFor, (BukkitUser) requester, text));
    }

    @Override
    public CompletableFuture<List<String>> findServers() {
        throw new UnsupportedOperationException("Cannot fetch the list of servers from a backend Bukkit server.");
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        if (exceptions.length > 0) {
            getLogger().log(level, message, exceptions[0]);
        } else {
            getLogger().log(level, message);
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        this.handlePluginMessage(this, channel, message);
    }

    @NotNull
    public final String formatPlaceholders(@NotNull UUID formatFor, @NotNull BukkitUser requester, @NotNull String text) {
        return formatter.formatPlaceholders(formatFor, requester.getPlayer(), text);
    }

}