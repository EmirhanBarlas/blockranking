package org.splendid.season;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.splendid.season.DatabaseManager;
import org.splendid.season.BlockRankingPlugin;
import org.splendid.season.BlockRankingPlaceholder;
import org.splendid.season.BlockBreakListener;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BlockRankingCommand implements CommandExecutor {

    private final Main plugin;

    public BlockRankingCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("blockranking") && sender instanceof Player) {
            Player player = (Player) sender;

            // Kullanıcının izni kontrol et
            if (!player.hasPermission("blockranking.use")) {
                String noPermissionMessage = plugin.getConfig().getString("no-permission-message");
                player.sendMessage(noPermissionMessage != null ? Colorize(noPermissionMessage) : "&cYou do not have permission to use this command.");
                return true;
            }

            // En fazla blok kıran oyuncuları al
            List<Map.Entry<UUID, Integer>> topPlayers = plugin.getBlockBreakCounts().entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(3)
                    .collect(Collectors.toList());

            // Mesajı oluştur
            StringBuilder message = new StringBuilder();
            String header = plugin.getConfig().getString("block-ranking-format.header", "&6Top 3 Block Breakers:");
            message.append(Colorize(header)).append("\n");

            for (int i = 0; i < topPlayers.size(); i++) {
                UUID playerUUID = topPlayers.get(i).getKey();
                Player topPlayer = plugin.getServer().getPlayer(playerUUID);
                String playerName = topPlayer != null ? topPlayer.getName() : "Unknown Player";
                int blocksBroken = topPlayers.get(i).getValue();

                String entryFormat = plugin.getConfig().getString("block-ranking-format.entry", "&e{rank}. {player}: {blocks} blocks");
                String entry = entryFormat
                        .replace("{rank}", String.valueOf(i + 1))
                        .replace("{player}", playerName)
                        .replace("{blocks}", String.valueOf(blocksBroken));

                message.append(Colorize(entry)).append("\n");
            }

            // Mesajı gönder
            player.sendMessage(message.toString());

            return true;
        }

        return false;
    }

    private String Colorize(String message) {
        return message.replace('&', '\u00A7'); // Renklendirme işlemi
    }
}
