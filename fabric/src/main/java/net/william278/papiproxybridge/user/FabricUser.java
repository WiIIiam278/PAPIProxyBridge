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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
//#if MC>=260102
import com.mojang.serialization.JsonOps;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
//#elseif MC>=12107
//$$ import net.minecraft.text.TextCodecs;
//$$ import com.mojang.serialization.JsonOps;
//$$ import net.minecraft.server.network.ServerPlayerEntity;
//$$ import net.minecraft.text.Text;
//$$ import net.minecraft.util.Language;
//#else
//$$ import net.minecraft.registry.DynamicRegistryManager;
//$$ import net.minecraft.server.network.ServerPlayerEntity;
//$$ import net.minecraft.text.Text;
//$$ import net.minecraft.util.Language;
//#endif
import net.william278.papiproxybridge.FabricPAPIProxyBridge;
import net.william278.papiproxybridge.PAPIProxyBridge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

//#if MC>=260102
public record FabricUser(ServerPlayer player) implements OnlineUser {
//#else
//$$ public record FabricUser(ServerPlayerEntity player) implements OnlineUser {
//#endif

    @NotNull
//#if MC>=260102
    public static FabricUser adapt(@NotNull ServerPlayer player) {
//#else
//$$ public static FabricUser adapt(@NotNull ServerPlayerEntity player) {
//#endif
        return new FabricUser(player);
    }

    @Override
    @NotNull
    public String getUsername() {
        return player.getName().getString();
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
//#if MC>=260102
        return player.getUUID();
//#else
//$$    return player.getUuid();
//#endif
    }

//#if MC>=260102
    private Component getComponent(net.minecraft.network.chat.Component text) {
        return GsonComponentSerializer.gson().deserialize(ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow().getAsString());
//#elseif MC>=12107
//$$ private Component getComponent(Text text) {
//$$    return GsonComponentSerializer.gson().deserialize(TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow().getAsString());
//#else
//$$ private Component getComponent(Text text) {
//$$      return GsonComponentSerializer.gson().deserialize(Text.Serialization.toJsonString(text, new DynamicRegistryManager.ImmutableImpl(List.of())));
//#endif
    }

    private Component translateKeys(TranslatableComponent translatable) {
        final String key = translatable.key();
        final @Nullable String translated = Objects.requireNonNullElse(
//#if MC>=260102
                Language.getInstance().getOrDefault(key, translatable.fallback()),
//#else
//$$            Language.getInstance().get(key, translatable.fallback()),
//#endif
                key
        );
        return translatable.fallback(translated);
    }

    @Override
    public void handleMessage(@NotNull PAPIProxyBridge plugin, @NotNull Request message, boolean wantsJson) {
        FabricPAPIProxyBridge bridge = (FabricPAPIProxyBridge) plugin;
//#if MC>=260102
        net.minecraft.network.chat.Component formatted = bridge.formatPlaceholders(message.getFormatFor(), this, message.getMessage());
//#else
//$$    Text formatted = bridge.formatPlaceholders(message.getFormatFor(), this, message.getMessage());
//#endif
        String response;

        if (wantsJson) {
            Component original = getComponent(formatted);
            Component transformed = original.children().stream().map(component -> {
                if (component instanceof TranslatableComponent trans) {
                    return translateKeys(trans);
                }
                return component;
            }).collect(Component.toComponent()).mergeStyle(original);
            response = GsonComponentSerializer.gson().serialize(transformed);
        } else {
            response = formatted.getString();
        }

        message.setMessage(response);
        this.sendMessage(plugin, message, wantsJson, false);
    }
}
