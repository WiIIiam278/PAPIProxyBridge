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
import lombok.Getter;
import lombok.Setter;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.config.Settings;
import net.william278.papiproxybridge.messenger.Messenger;
import net.william278.papiproxybridge.messenger.PluginMessageMessenger;
import net.william278.papiproxybridge.messenger.redis.RedisMessenger;
import net.william278.papiproxybridge.papi.Formatter;
import net.william278.papiproxybridge.user.BukkitUser;
import net.william278.papiproxybridge.user.OnlineUser;
import org.bstats.bukkit.Metrics;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

@Getter
public class BukkitPAPIProxyBridge extends JavaPlugin implements PAPIProxyBridge, Listener {

    private Formatter formatter;
    private Map<UUID, BukkitUser> users;
    private Map<String, BukkitUser> usersByName;
    @Setter
    private Settings settings;
    private Messenger messenger;
    private ExecutorService executorService;

    @Override
    public void onLoad() {
        users = Maps.newConcurrentMap();
        usersByName = Maps.newConcurrentMap();
        executorService = Executors.newFixedThreadPool(2);
        // Initialize the formatter
        formatter = new Formatter();
    }

    @Override
    public void onEnable() {
        loadConfig();
        loadMessenger();
        messenger.onEnable();
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
        messenger.onDisable();
    }

    private void loadOnlinePlayers() {
        users.clear();
        getServer().getOnlinePlayers().forEach(player -> {
            final BukkitUser user = BukkitUser.adapt(player);
            users.put(player.getUniqueId(), user);
            usersByName.put(user.getUsername(), user);
        });
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
        return Optional.ofNullable(usersByName.get(username));
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

    @NotNull
    public final CompletableFuture<String> formatPlaceholders(@NotNull UUID formatFor, @NotNull BukkitUser requester, @NotNull String text) {
        return CompletableFuture.supplyAsync(() -> formatter.formatPlaceholders(formatFor, requester.player(), text), executorService);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final BukkitUser user = BukkitUser.adapt(event.getPlayer());
        users.put(user.getUniqueId(), user);
        usersByName.put(user.getUsername(), user);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        users.remove(event.getPlayer().getUniqueId());
        usersByName.remove(event.getPlayer().getName());
    }

    @Override
    public void loadMessenger() {
        switch (settings.getMessenger()) {
            case REDIS -> messenger = new RedisMessenger(this, settings.getRedis(), true);
            case PLUGIN_MESSAGE -> messenger = new PluginMessageMessenger(this);
        }
        log(Level.INFO, "Loaded messenger " + messenger.getClass().getSimpleName());
    }
}