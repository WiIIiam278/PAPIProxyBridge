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
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.config.Settings;
import net.william278.papiproxybridge.messenger.Messenger;
import net.william278.papiproxybridge.messenger.PluginMessageMessenger;
import net.william278.papiproxybridge.messenger.redis.RedisMessenger;
import net.william278.papiproxybridge.user.BungeeUser;
import org.bstats.bungeecord.Metrics;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class BungeePAPIProxyBridge extends Plugin implements ProxyPAPIProxyBridge, Listener {

    private ConcurrentMap<UUID, CompletableFuture<String>> requests;
    private Map<UUID, BungeeUser> users;
    private Settings settings;
    private Messenger messenger;

    @Override
    public void onEnable() {
        requests = Maps.newConcurrentMap();
        users = Maps.newConcurrentMap();
        loadConfig();
        loadMessenger();
        messenger.onEnable();

        // Register the plugin message listener
        getProxy().getPluginManager().registerListener(this, this);

        // Register the plugin with the API
        PlaceholderAPI.register(this);

        // Metrics
        new Metrics(this, 17879);

        getLogger().info(getLoadMessage());
    }

    @Override
    public void onDisable() {
        messenger.onDisable();
        // Unregister the plugin message listener
        getProxy().getPluginManager().unregisterListener(this);
    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent event) {
        this.handleMessage(this, event.getTag(), event.getData(), false);
    }

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        final BungeeUser user = BungeeUser.adapt(event.getPlayer());
        users.put(user.getUniqueId(), user);
    }

    @EventHandler
    public void onQuit(PostLoginEvent event) {
        final BungeeUser user = BungeeUser.adapt(event.getPlayer());
        users.remove(user.getUniqueId());
        PlaceholderAPI.clearCache(event.getPlayer().getUniqueId());
    }

    @Override
    @NotNull
    public ConcurrentMap<UUID, CompletableFuture<String>> getRequests() {
        return requests;
    }

    @Override
    public String getServerType() {
        return "BungeeCord";
    }

    @Override
    @NotNull
    public Collection<BungeeUser> getOnlineUsers() {
        return users.values();
    }

    @Override
    public Optional<BungeeUser> findPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    @Override
    public Optional<BungeeUser> findPlayer(@NotNull String username) {
        return users.values()
                .stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
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
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void loadMessenger() {
        switch (settings.getMessenger()) {
            case REDIS -> messenger = new RedisMessenger(this, settings.getRedis(), false);
            case PLUGIN_MESSAGE -> messenger = new PluginMessageMessenger(this);
        }

        log(Level.INFO, "Loaded messenger " + messenger.getClass().getSimpleName());
    }

    @Override
    public Messenger getMessenger() {
        return messenger;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }
}
