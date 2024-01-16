package org.splendid.season;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {

    private final Main plugin;
    private Map<UUID, Integer> playerData; // Oyuncu verileri
    private Connection connection;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        this.playerData = new HashMap<>();
        this.connection = initializeDatabase();
        loadPlayerData(); // Sunucu başladığında mevcut verileri yükle
        startAutoSaveTask(); // Otomatik kaydetme görevini başlat
    }
    public String getPlayerName(UUID playerUUID) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT player_name FROM player_data WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("player_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    private Connection initializeDatabase() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath() + File.separator + "database.db");

            // player_data tablosunu oluştur
            try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS player_data (player_uuid VARCHAR(36) PRIMARY KEY, block_count INT)")) {
                statement.executeUpdate();
            }

            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getPlayerData(UUID playerUUID) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT block_count FROM player_data WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("block_count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void savePlayerData(UUID playerUUID, int blockCount) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO player_data (player_uuid, block_count) VALUES (?, ?)")) {
            statement.setString(1, playerUUID.toString());
            statement.setInt(2, blockCount);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPlayerData() {
        if (connection == null) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_data");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
                int blockCount = resultSet.getInt("block_count");
                playerData.put(uuid, blockCount);
            }

            plugin.getLogger().info("Veriler başarıyla yüklendi.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Veriler yüklenirken hata oluştu! Error: " + e.getMessage());
        }
    }

    private void startAutoSaveTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveDataToDatabase();
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 20L); // Her saniye (20 tick = 1 saniye)
    }

    private void saveDataToDatabase() {
        if (connection == null) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement("REPLACE INTO player_data (player_uuid, block_count) VALUES (?, ?)")) {
            for (Map.Entry<UUID, Integer> entry : playerData.entrySet()) {
                UUID uuid = entry.getKey();
                int blockCount = entry.getValue();

                statement.setString(1, uuid.toString());
                statement.setInt(2, blockCount);
                statement.addBatch();
            }

            statement.executeBatch();
            plugin.getLogger().info("Veriler başarıyla veritabanına kaydedildi.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Veriler veritabanına kaydedilirken hata oluştu! Error: " + e.getMessage());
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Veritabanı bağlantısı kapatıldı.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Veritabanı bağlantısı kapatılırken hata oluştu! Error: " + e.getMessage());
            }
        }
    }
}
