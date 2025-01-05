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

import com.google.common.collect.Maps;
import io.github.projectunified.minelib.scheduler.entity.EntityScheduler;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.config.Settings;
import net.william278.papiproxybridge.messenger.Messenger;
import net.william278.papiproxybridge.messenger.PluginMessageMessenger;
import net.william278.papiproxybridge.messenger.redis.RedisMessenger;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BukkitPAPIProxyBridge extends JavaPlugin implements PAPIProxyBridge, PluginMessageListener, Listener {

    private Formatter formatter;
    private Map<UUID, BukkitUser> users;
    private Settings settings;
    private Messenger messenger;

    @Override
    public void onLoad() {
        users = Maps.newConcurrentMap();
        // Initialize the formatter
        formatter = new Formatter();
    }

    @Override
    public void onEnable() {
        loadConfig();
        // Register the plugin message channel


        // Register the plugin with the API
        PlaceholderAPI.register(this);

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Load online players
        loadOnlinePlayers();

        // Metrics
        new Metrics(this, 17880);

        getLogger().info(getLoadMessage());
    }

    @Override
    public void onDisable() {
        // Unregister the plugin message channel
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    private void loadOnlinePlayers() {
        users.clear();
        getServer().getOnlinePlayers().forEach(player -> users.put(player.getUniqueId(), BukkitUser.adapt(player)));
    }

    @Override
    public String getServerType() {
        return getServer().getName();
    }

    @Override
    @NotNull
    public Collection<BukkitUser> getOnlineUsers() {
        return users.values();
    }

    @Override
    public Optional<BukkitUser> findPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    @Override
    public Optional<BukkitUser> findPlayer(@NotNull String username) {
        return users.values().stream()
                .filter(user -> user.getPlayer().getName().equals(username))
                .findFirst();
    }

    @Override
    public CompletableFuture<String> createRequest(@NotNull String text, @NotNull OnlineUser requester, @NotNull UUID formatFor, boolean wantsJson, long requestTimeout) {
        return formatPlaceholders(formatFor, (BukkitUser) requester, text);
    }

    @Override
    public CompletableFuture<Set<String>> getServers(long requestTimeout) {
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
        this.handleMessage(this, channel, message);
    }

    @NotNull
    public final CompletableFuture<String> formatPlaceholders(@NotNull UUID formatFor, @NotNull BukkitUser requester, @NotNull String text) {
        final CompletableFuture<String> future = new CompletableFuture<>();
        EntityScheduler.get(this, requester.getPlayer()).runLater(
                () -> future.complete(formatter.formatPlaceholders(formatFor, requester.getPlayer(), text)),
                requester.justSwitchedServer() ? 2 : 1);
        return future;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final BukkitUser user = BukkitUser.adapt(event.getPlayer());
        user.setJustSwitchedServer(true);
        users.put(user.getUniqueId(), user);
        EntityScheduler.get(this, user.getPlayer()).runLater(
                () -> user.setJustSwitchedServer(false),
                20);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        users.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void loadMessenger() {
        switch (settings.getMessenger()) {
            case REDIS -> messenger = new RedisMessenger(this, settings.getRedis());
            case PLUGIN_MESSAGE -> messenger = new PluginMessageMessenger(this);
        }
    }

    @Override
    public Messenger getMessenger() {
        return messenger;
    }
}