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

import com.google.common.collect.Lists;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.papi.Formatter;
import net.william278.papiproxybridge.user.BukkitUser;
import net.william278.papiproxybridge.user.OnlineUser;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import com.tcoded.folialib.FoliaLib;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BukkitPAPIProxyBridge extends JavaPlugin implements PAPIProxyBridge, PluginMessageListener, Listener {
    private Formatter formatter;
    private FoliaLib foliaLib;
    private final List<BukkitUser> users = Lists.newCopyOnWriteArrayList();

    @Override
    public void onLoad() {
        // Initialize the formatter
        formatter = new Formatter();
        FoliaLib foliaLib = new FoliaLib(this);
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

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Load online players
        loadOnlinePlayers();

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

    private void loadOnlinePlayers() {
        users.clear();
        getServer().getOnlinePlayers().forEach(player -> users.add(BukkitUser.adapt(player)));
    }

    @Override
    @NotNull
    public List<BukkitUser> getOnlineUsers() {
        return users;
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull UUID uuid) {
        return users.stream().filter(user -> user.getUniqueId().equals(uuid)).map(u -> (OnlineUser) u).findFirst();
    }

    @Override
    public Optional<OnlineUser> findPlayer(@NotNull String username) {
        return users.stream().filter(user -> user.getUsername().equals(username)).map(u -> (OnlineUser) u).findFirst();
    }

    @Override
    public CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser requester, @NotNull UUID formatFor, boolean wantsJson, long requestTimeout) {
        return formatPlaceholders(formatFor, (BukkitUser) requester, text);
    }

    @Override
    public CompletableFuture<List<String>> findServers(long requestTimeout) {
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
    public final CompletableFuture<String> formatPlaceholders(@NotNull UUID formatFor, @NotNull BukkitUser requester, @NotNull String text) {
        final CompletableFuture<String> future = new CompletableFuture<>();
        if (!foliaLib.isFolia()) {
            getServer().getScheduler().runTaskLater(this,
                    () -> future.complete(formatter.formatPlaceholders(formatFor, requester.getPlayer(), text)),
                    requester.justSwitchedServer() ? 2 : 1);
        } else {
            foliaLib.getImpl().runLater(
                    () -> future.complete(formatter.formatPlaceholders(formatFor, requester.getPlayer(), text)),
                    requester.justSwitchedServer() ? 2 : 1);
        }
        return future;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final BukkitUser user = BukkitUser.adapt(event.getPlayer());
        user.setJustSwitchedServer(true);
        users.add(user);
        if (!foliaLib.isFolia()) {
            getServer().getScheduler().runTaskLater(this,
                    () -> user.setJustSwitchedServer(false),
                    10);
        } else {
            foliaLib.getImpl().runLater(() -> user.setJustSwitchedServer(false),
                    10);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        users.removeIf(user -> user.getUniqueId().equals(event.getPlayer().getUniqueId()));
    }

}