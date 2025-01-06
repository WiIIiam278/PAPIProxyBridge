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

package net.william278.papiproxybridge.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

@Configuration
@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class Settings {

    @Comment("The messenger to use for sending plugin messages. Options are PLUGIN_MESSAGE or REDIS.")
    private MessengerType messenger = MessengerType.PLUGIN_MESSAGE;
    private RedisSettings redis = new RedisSettings("localhost", 6379, "");

    public MessengerType getMessenger() {
        return messenger;
    }

    public RedisSettings getRedis() {
        return redis;
    }

    public enum MessengerType {
        PLUGIN_MESSAGE,
        REDIS
    }

    public record RedisSettings(String host, int port, String password) {
    }

}
