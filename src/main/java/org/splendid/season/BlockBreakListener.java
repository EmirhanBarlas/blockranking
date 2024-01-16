package org.splendid.season;

import org.splendid.season.DatabaseManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.splendid.season.DatabaseManager;
import org.splendid.season.BlockRankingPlugin;
import org.splendid.season.BlockRankingPlaceholder;
import org.splendid.season.BlockBreakListener;
import java.util.UUID;

public class BlockBreakListener implements Listener {

    private final DatabaseManager databaseManager;

    public BlockBreakListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        int blockCount = databaseManager.getPlayerData(playerUUID) + 1; // Oyuncunun blok sayısını bir arttır
        databaseManager.savePlayerData(playerUUID, blockCount);
    }
}