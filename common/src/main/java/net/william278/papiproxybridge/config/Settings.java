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

import com.google.common.collect.Lists;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Configuration
@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class Settings {

    @Comment("The messenger to use for sending plugin messages. Options are PLUGIN_MESSAGE or REDIS.")
    private MessengerType messenger = MessengerType.PLUGIN_MESSAGE;
    private RedisSettings redis = new RedisSettings();

    public enum MessengerType {
        PLUGIN_MESSAGE,
        REDIS
    }

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RedisSettings {

        @Comment("Specify the credentials of your Redis server here. Set \"password\" to '' if you don't have one")
        private RedisCredentials credentials = new RedisCredentials();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class RedisCredentials {
            private String host = "localhost";
            private int port = 6379;
            private String password = "";
            private boolean useSsl = false;
        }

        @Comment("Options for if you're using Redis sentinel. Don't modify this unless you know what you're doing!")
        private RedisSentinel sentinel = new RedisSentinel();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class RedisSentinel {
            @Comment("The master set name for the Redis sentinel.")
            private String master = "";
            @Comment("List of host:port pairs")
            private List<String> nodes = Lists.newArrayList();
            private String password = "";
        }

    }
}
