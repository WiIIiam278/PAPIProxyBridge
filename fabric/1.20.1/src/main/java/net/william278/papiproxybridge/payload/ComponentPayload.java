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

package net.william278.papiproxybridge.payload;

import net.minecraft.util.Identifier;
import net.william278.papiproxybridge.PAPIProxyBridge;

public final class ComponentPayload extends TemplatePayload {

    public static final Identifier RESPONSE_ID = new Identifier(PAPIProxyBridge.getComponentChannel(false));
    public static final Identifier REQUEST_ID = new Identifier(PAPIProxyBridge.getComponentChannel(true));

    private final boolean isRequest;

    public ComponentPayload(byte[] bytes, boolean isRequest) {
        super(bytes);
        this.isRequest = isRequest;
    }

    public Identifier getId() {
        return isRequest ? REQUEST_ID : RESPONSE_ID;
    }
}