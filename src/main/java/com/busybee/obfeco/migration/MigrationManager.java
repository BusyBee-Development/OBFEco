package com.busybee.obfeco.migration;

import com.busybee.obfeco.Obfeco;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class MigrationManager {
    private final Obfeco plugin;

    public interface MigrationCallback {
        void onComplete(boolean success, int count, String error);
    }

    public void migrate(String source, String targetCurrency, MigrationCallback callback) {
        if (source.equalsIgnoreCase("coinsengine")) {
            migrateCoinsEngine(targetCurrency, callback);
        } else if (source.equalsIgnoreCase("vault")) {
            migrateVault(targetCurrency, callback);
        } else {
            callback.onComplete(false, 0, "Unsupported source: " + source);
        }
    }

    private void migrateCoinsEngine(String targetCurrency, MigrationCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String host = plugin.getConfig().getString("migration.coinsengine.host", "localhost");
                int port = plugin.getConfig().getInt("migration.coinsengine.port", 3306);
                String database = plugin.getConfig().getString("migration.coinsengine.database", "minecraft");
                String user = plugin.getConfig().getString("migration.coinsengine.username", "root");
                String pass = plugin.getConfig().getString("migration.coinsengine.password", "password");
                String table = plugin.getConfig().getString("migration.coinsengine.table", "coinsengine_data");
                String sourceCurrency = plugin.getConfig().getString("migration.coinsengine.source-currency", targetCurrency);

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";

                Map<UUID, Double> data = new HashMap<>();
                try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                    ResultSetMetaData meta;
                    boolean hasUuidColumn = false;
                    boolean hasPlayerUuidColumn = false;
                    boolean hasCurrencyIdColumn = false;
                    boolean hasBalanceColumn = false;

                    try (PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM " + table + " LIMIT 1");
                         ResultSet checkRs = checkStmt.executeQuery()) {
                        meta = checkRs.getMetaData();
                        int colCount = meta.getColumnCount();
                        for (int i = 1; i <= colCount; i++) {
                            String colName = meta.getColumnName(i).toLowerCase();
                            if (colName.equals("uuid")) hasUuidColumn = true;
                            if (colName.equals("player_uuid")) hasPlayerUuidColumn = true;
                            if (colName.equals("currency_id") || colName.equals("currency")) hasCurrencyIdColumn = true;
                            if (colName.equals("balance") || colName.equals("amount")) hasBalanceColumn = true;
                        }
                    }

                    String uuidCol = hasUuidColumn ? "uuid" : (hasPlayerUuidColumn ? "player_uuid" : "uuid");

                    if (!hasBalanceColumn) {
                        callback.onComplete(false, 0, "Source table '" + table + "' has no 'balance' or 'amount' column. Check table name and structure.");
                        return;
                    }

                    String balanceCol = "balance";
                    try (PreparedStatement testStmt = conn.prepareStatement("SELECT " + balanceCol + " FROM " + table + " LIMIT 1")) {
                        testStmt.executeQuery();
                    } catch (Exception e) {
                        balanceCol = "amount";
                    }

                    String query;
                    if (hasCurrencyIdColumn) {
                        String currencyCol = "currency_id";
                        try (PreparedStatement testStmt = conn.prepareStatement("SELECT currency_id FROM " + table + " LIMIT 1")) {
                            testStmt.executeQuery();
                        } catch (Exception e) {
                            currencyCol = "currency";
                        }
                        query = "SELECT " + uuidCol + ", " + balanceCol + " FROM " + table + " WHERE " + currencyCol + " = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(query)) {
                            stmt.setString(1, sourceCurrency);
                            ResultSet rs = stmt.executeQuery();
                            while (rs.next()) {
                                try {
                                    UUID uuid = UUID.fromString(rs.getString(uuidCol));
                                    double balance = rs.getDouble(balanceCol);
                                    data.put(uuid, balance);
                                } catch (IllegalArgumentException ignored) {
                                }
                            }
                        }
                    } else {
                        query = "SELECT " + uuidCol + ", " + balanceCol + " FROM " + table;
                        try (PreparedStatement stmt = conn.prepareStatement(query)) {
                            ResultSet rs = stmt.executeQuery();
                            while (rs.next()) {
                                try {
                                    UUID uuid = UUID.fromString(rs.getString(uuidCol));
                                    double balance = rs.getDouble(balanceCol);
                                    data.put(uuid, balance);
                                } catch (IllegalArgumentException ignored) {
                                }
                            }
                        }
                    }
                }

                if (!data.isEmpty()) {
                    plugin.getDatabaseManager().createCurrencyTable(targetCurrency);
                    plugin.getDatabaseManager().batchSetBalances(targetCurrency, data);
                    Bukkit.getScheduler().runTask(plugin, () -> callback.onComplete(true, data.size(), null));
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.onComplete(false, 0, "No data found in table '" + table + "' for currency: " + sourceCurrency));
                }

            } catch (Exception e) {
                plugin.getLogger().severe("Migration error: " + e.getMessage());
                e.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> callback.onComplete(false, 0, e.getMessage()));
            }
        });
    }

    private void migrateVault(String targetCurrency, MigrationCallback callback) {
        callback.onComplete(false, 0, "Vault migration not yet implemented");
    }
}