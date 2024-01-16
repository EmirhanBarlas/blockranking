package org.splendid.season;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockRankingPlugin extends JavaPlugin implements Listener {

    private final Map<UUID, Integer> blockBreakCounts = new HashMap<>();
    private final File dataFile = new File(getDataFolder(), "data.yml");
    private final DatabaseManager databaseManager;

    public BlockRankingPlugin(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void onEnable() {
        // Diğer başlangıç işlemleri...
        getServer().getPluginManager().registerEvents(this, this);
        loadData(); // Data dosyasındaki verileri yükle
        scheduleMonthlyReset();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Oyuncunun blok kırma sayısını güncelle
        blockBreakCounts.put(playerUUID, blockBreakCounts.getOrDefault(playerUUID, 0) + 1);

        // Data dosyasına güncellenmiş verileri kaydet
        saveData();
    }

    private void scheduleMonthlyReset() {
        // Ayın 2. günü saat 00:00'da sıfırlama işlemi
        // Burada sıfırlama işlemleri yapılabilir
    }

    private void resetBlockBreakCounts() {
        blockBreakCounts.clear();
    }

    private void saveData() {
        // Data dosyasına blok kırma verilerini kaydet
        try {
            if (!dataFile.exists()) {
                dataFile.createNewFile();
            }
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(dataFile);
            for (Map.Entry<UUID, Integer> entry : blockBreakCounts.entrySet()) {
                String playerName = databaseManager.getPlayerName(entry.getKey()); // DataManager'dan ismi al
                yamlConfiguration.set("players." + entry.getKey().toString() + ".name", playerName);
                yamlConfiguration.set("players." + entry.getKey().toString() + ".blocksBroken", entry.getValue());
            }
            yamlConfiguration.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        // Data dosyasından blok kırma verilerini yükle
        if (dataFile.exists()) {
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(dataFile);
            ConfigurationSection playersSection = yamlConfiguration.getConfigurationSection("players");
            if (playersSection != null) {
                for (String playerUUID : playersSection.getKeys(false)) {
                    String playerName = playersSection.getString(playerUUID + ".name");
                    int blocksBroken = playersSection.getInt(playerUUID + ".blocksBroken");
                    UUID uuid = UUID.fromString(playerUUID);
                    blockBreakCounts.put(uuid, blocksBroken);
                }
            }
        }
    }
}
