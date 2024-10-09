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
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.user.VelocityUser;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Plugin(id = "papiproxybridge")
@SuppressWarnings("unused")
public class VelocityPAPIProxyBridge implements ProxyPAPIProxyBridge {

    private final ConcurrentMap<UUID, CompletableFuture<String>> requests;
    private final ChannelIdentifier channelIdentifier;
    private final ChannelIdentifier componentChannelIdentifier;
    private final ProxyServer server;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;
    private final Map<UUID, VelocityUser> velocityUsers;

    @Inject
    public VelocityPAPIProxyBridge(ProxyServer server, org.slf4j.Logger logger, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.requests = Maps.newConcurrentMap();
        this.channelIdentifier = new LegacyChannelIdentifier(getChannel());
        this.componentChannelIdentifier = new LegacyChannelIdentifier(getComponentChannel());
        this.velocityUsers = Maps.newConcurrentMap();
    }

    private void loadOnlineUsers() {
        velocityUsers.clear();
        server.getAllPlayers().forEach(this::loadPlayer);
    }

    private void loadPlayer(@NotNull Player player) {
        final VelocityUser user = VelocityUser.adapt(player);
        user.setJustSwitchedServer(true);
        velocityUsers.put(player.getUniqueId(), user);
        server.getScheduler().buildTask(this, () -> user.setJustSwitchedServer(false)).delay(2000, TimeUnit.MILLISECONDS).schedule();
    }

    @Subscribe
    public void onProxyInitialization(@NotNull ProxyInitializeEvent event) {
        // Register the plugin message channel
        this.loadOnlineUsers();
        server.getChannelRegistrar().register(this.channelIdentifier);
        server.getChannelRegistrar().register(this.componentChannelIdentifier);

        // Register the plugin with the API
        PlaceholderAPI.register(this);

        // Setup metrics
        metricsFactory.make(this, 17878);

        logger.info(getLoadMessage());
    }

    @Subscribe
    public void onPluginMessageReceived(@NotNull PluginMessageEvent event) {
        ChannelIdentifier channelId = event.getIdentifier();
        if (!channelId.equals(this.channelIdentifier) && !channelId.equals(this.componentChannelIdentifier)) {
            return;
        }

        handlePluginMessage(this, event.getIdentifier().getId(), event.getData());
        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }

    @Subscribe
    public void onServerChange(@NotNull ServerConnectedEvent event) {
        findPlayer(event.getPlayer().getUniqueId()).ifPresent(user -> {
            final VelocityUser velocityUser = (VelocityUser) user;
            velocityUser.setJustSwitchedServer(true);

            server.getScheduler().buildTask(this,
                            () -> velocityUser.setJustSwitchedServer(false))
                    .delay(1, TimeUnit.SECONDS).schedule();
        });
    }

    @Subscribe
    public void onConnect(LoginEvent event) {
        loadPlayer(event.getPlayer());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        velocityUsers.remove(event.getPlayer().getUniqueId());
        PlaceholderAPI.clearCache(event.getPlayer().getUniqueId());
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        if (exceptions.length > 0) {
            logger.error(message, exceptions[0]);
        } else {
            logger.info(message);
        }
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
    public Optional<VelocityUser> findPlayer(@NotNull String username) {
        return server.getPlayer(username).map(this::getPlayer);
    }
}
