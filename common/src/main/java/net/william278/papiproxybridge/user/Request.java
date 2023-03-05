package net.william278.papiproxybridge.user;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class Request {
    private final UUID uuid;
    private String message;

    public Request(@NotNull String message) {
        this.uuid = UUID.randomUUID();
        this.message = message;
    }

    private Request(@NotNull UUID uuid, @NotNull String message) {
        this.uuid = uuid;
        this.message = message;
    }

    @Override
    public String toString() {
        return uuid.toString() + message;
    }

    @NotNull
    public static Request fromString(@NotNull String string) {
        return new Request(UUID.fromString(string.substring(0, 36)), string.substring(36));
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    public void setMessage(@NotNull String message) {
        this.message = message;
    }

}
