package org.splendid.season;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import org.splendid.season.DatabaseManager;
import org.splendid.season.BlockRankingPlugin;
import org.splendid.season.BlockRankingPlaceholder;
import org.splendid.season.BlockBreakListener;
public class BlockRankingPlaceholder extends PlaceholderExpansion {

    private final Main plugin;

    public BlockRankingPlaceholder(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "blockranking";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("topplayer")) {
            return getTopPlayer(player.getUniqueId());
        }

        return null;
    }

    private String getTopPlayer(UUID playerUUID) {
        // PlaceholderAPI için sıralama metodu
        Map.Entry<UUID, Integer> topEntry = null;

        for (Map.Entry<UUID, Integer> entry : plugin.getBlockBreakCounts().entrySet()) {
            if (topEntry == null || entry.getValue() > topEntry.getValue()) {
                topEntry = entry;
            }
        }

        if (topEntry != null) {
            Player topPlayer = plugin.getServer().getPlayer(topEntry.getKey());
            if (topPlayer != null) {
                return topPlayer.getName() + " - " + topEntry.getValue() + " blok";
            }
        }

        return "Henüz kimse blok kırmadı.";
    }
}
