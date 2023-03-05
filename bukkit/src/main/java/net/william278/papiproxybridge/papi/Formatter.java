package net.william278.papiproxybridge.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Formatter {

    @NotNull
    public final String formatPlaceholders(@NotNull Player player, @NotNull String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

}
