package org.splendid.season;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin {

    private final Map<UUID, Integer> blockBreakCounts = new HashMap<>();
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        // DatabaseManager'ı oluştur
        databaseManager = new DatabaseManager(this);

        // Komut ve event işlemlerini başlat
        registerCommands();
        registerEvents();

        // PlaceholderAPI entegrasyonunu kontrol et
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            // PlaceholderAPI ile sıralama placeholder'ını kaydet
            new BlockRankingPlaceholder(this).register();
        } else {
            getLogger().warning("PlaceholderAPI bulunamadı! Eklentinin düzgün çalışması için PlaceholderAPI gereklidir.");
        }
    }

    @Override
    public void onDisable() {
        // Eklenti kapatıldığında veritabanı bağlantısını kapat
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
    }

    private void registerCommands() {
        // Komutları kaydet
        getCommand("blockranking").setExecutor(new BlockRankingCommand(this));
    }

    private void registerEvents() {
        // Event dinleyicilerini kaydet
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BlockBreakListener(databaseManager), this);
    }

    public Map<UUID, Integer> getBlockBreakCounts() {
        return blockBreakCounts;
    }
}
