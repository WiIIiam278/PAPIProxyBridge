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
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.Setter;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.config.Settings;
import net.william278.papiproxybridge.messenger.Messenger;
import net.william278.papiproxybridge.messenger.PluginMessageMessenger;
import net.william278.papiproxybridge.messenger.redis.RedisMessenger;
import net.william278.papiproxybridge.user.VelocityUser;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

@Plugin(id = "papiproxybridge")
@SuppressWarnings("unused")
@Getter
public class VelocityPAPIProxyBridge implements ProxyPAPIProxyBridge {

    private final ConcurrentMap<UUID, CompletableFuture<String>> requests;

    private final ProxyServer server;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;
    private final Path configDirectory;
    private final Map<UUID, VelocityUser> velocityUsers;
    @Setter
    private Settings settings;
    private Messenger messenger;

    @Inject
    public VelocityPAPIProxyBridge(ProxyServer server, org.slf4j.Logger logger, Metrics.Factory metricsFactory, @DataDirectory Path configDirectory) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.configDirectory = configDirectory;
        this.requests = Maps.newConcurrentMap();
        this.velocityUsers = Maps.newConcurrentMap();
    }

    private void loadOnlineUsers() {
        velocityUsers.clear();
        server.getAllPlayers().forEach(this::loadPlayer);
    }

    private void loadPlayer(@NotNull Player player) {
        final VelocityUser user = VelocityUser.adapt(player);
        velocityUsers.put(player.getUniqueId(), user);
    }

    @Subscribe
    public void onProxyInitialization(@NotNull ProxyInitializeEvent event) {
        loadConfig();
        loadMessenger();
        messenger.onEnable();
        loadOnlineUsers();


        // Register the plugin with the API
        PlaceholderAPI.register(this);

        // Setup metrics
        setupMetrics();

        logger.info(getLoadMessage());
    }

    @Subscribe
    public void onProxyShutdown(@NotNull ProxyShutdownEvent event) {
        messenger.onDisable();
    }

    @Subscribe
    public void onConnect(PostLoginEvent event) {
        loadPlayer(event.getPlayer());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if (event.getLoginStatus() == DisconnectEvent.LoginStatus.CONFLICTING_LOGIN) {
            return;
        }

        velocityUsers.remove(event.getPlayer().getUniqueId());
        PlaceholderAPI.clearCache(event.getPlayer().getUniqueId());
    }

    private void setupMetrics() {
        final Metrics metrics = metricsFactory.make(this, 17878);
        metrics.addCustomChart(new SimplePie("messengerType", () -> getSettings().getMessenger().name()));
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        if (exceptions.length > 0) {
            logger.info("Error: {}", message);
            logger.error(message, exceptions[0]);
        } else {
            if (level.equals(Level.SEVERE)) {
                logger.error(message);
            } else if (level.equals(Level.WARNING)) {
                logger.warn(message);
            } else {
                logger.info(message);
            }
        }
    }

    @Override
    public File getDataFolder() {
        return configDirectory.toFile();
    }

    @Override
    @NotNull
    public ConcurrentMap<UUID, CompletableFuture<String>> getRequests() {
        return requests;
    }

    @Override
    public String getServerType() {
        return "Velocity";
    }

    @Override
    @NotNull
    public Collection<VelocityUser> getOnlineUsers() {
        return velocityUsers.values();
    }

    @Override
    public Optional<VelocityUser> findPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(velocityUsers.get(uuid));
    }

    public VelocityUser getPlayer(@NotNull Player player) {
        return velocityUsers.get(player.getUniqueId());
    }

    @Override
    public void loadMessenger() {
        switch (settings.getMessenger()) {
            case REDIS -> messenger = new RedisMessenger(this, settings.getRedis(), false);
            case PLUGIN_MESSAGE -> messenger = new PluginMessageMessenger(this);
        }

        log(Level.INFO, "Loaded messenger " + getSettings().getMessenger().name());
    }
}
