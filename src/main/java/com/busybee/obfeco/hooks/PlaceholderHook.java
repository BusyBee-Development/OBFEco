package com.busybee.obfeco.hooks;

import com.busybee.obfeco.Obfeco;
import com.busybee.obfeco.core.Currency;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class PlaceholderHook extends PlaceholderExpansion {
    private final Obfeco plugin;
    private final Map<String, List<Map.Entry<UUID, Double>>> topCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastCacheUpdate = new ConcurrentHashMap<>();

    @Override
    public @NotNull String getIdentifier() {
        return "obfeco";
    }

    @Override
    public @NotNull String getAuthor() {
        return "BusyBee";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.isEmpty()) return null;

        String[] parts = params.split("_");
        if (parts.length == 0) return null;

        // Try to find the longest matching currency ID from the params
        String currencyId = null;
        Currency currency = null;

        for (int i = parts.length; i > 0; i--) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < i; j++) {
                if (j > 0) sb.append("_");
                sb.append(parts[j]);
            }
            String testId = sb.toString();
            Currency testCurrency = plugin.getCurrencyManager().getCurrency(testId);
            if (testCurrency != null) {
                currencyId = testId;
                currency = testCurrency;
                break;
            }
        }

        if (currency == null) return null;

        String remaining = params.substring(currencyId.length());
        if (remaining.startsWith("_")) remaining = remaining.substring(1);

        if (remaining.equalsIgnoreCase("formatted")) {
            if (player == null) return "0";
            try {
                double balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), currencyId).get();
                return plugin.getConfigManager().formatAmount(balance, currency);
            } catch (Exception e) {
                return "0";
            }
        }

        if (remaining.isEmpty() || remaining.equalsIgnoreCase("raw")) {
            if (player == null) return "0";
            try {
                double balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), currencyId).get();
                if (!currency.isUseDecimals()) {
                    return String.valueOf((long) Math.floor(balance));
                }
                return String.format("%." + plugin.getConfigManager().getDecimalPlaces() + "f", balance);
            } catch (Exception e) {
                return "0";
            }
        }

        if (remaining.equalsIgnoreCase("total_formatted")) {
            double total = plugin.getDatabaseManager().getTotalCurrencyValue(currencyId);
            return plugin.getConfigManager().formatAmount(total, currency);
        }

        if (remaining.equalsIgnoreCase("total") || remaining.equalsIgnoreCase("total_raw")) {
            double total = plugin.getDatabaseManager().getTotalCurrencyValue(currencyId);
            if (!currency.isUseDecimals()) {
                return String.valueOf((long) Math.floor(total));
            }
            return String.format("%." + plugin.getConfigManager().getDecimalPlaces() + "f", total);
        }

        if (remaining.toLowerCase().startsWith("top_")) {
            String[] topParts = remaining.split("_");
            if (topParts.length >= 3) {
                String type = topParts[1];
                int position;
                try {
                    position = Integer.parseInt(topParts[2]);
                } catch (NumberFormatException e) {
                    return null;
                }

                updateTopCache(currencyId);
                List<Map.Entry<UUID, Double>> topList = topCache.get(currencyId);

                if (topList == null || position < 1 || position > topList.size()) {
                    return type.equalsIgnoreCase("name") ? "---" : "0";
                }

                Map.Entry<UUID, Double> entry = topList.get(position - 1);
                if (type.equalsIgnoreCase("name")) {
                    OfflinePlayer topPlayer = Bukkit.getOfflinePlayer(entry.getKey());
                    return topPlayer.getName() != null ? topPlayer.getName() : "Unknown";
                } else if (type.equalsIgnoreCase("value") || type.equalsIgnoreCase("formatted")) {
                    return plugin.getConfigManager().formatAmount(entry.getValue(), currency);
                } else if (type.equalsIgnoreCase("rawvalue") || type.equalsIgnoreCase("raw")) {
                    if (!currency.isUseDecimals()) {
                        return String.valueOf((long) Math.floor(entry.getValue()));
                    }
                    return String.format("%." + plugin.getConfigManager().getDecimalPlaces() + "f", entry.getValue());
                }
            }
            return null;
        }

        return null;
    }

    private void updateTopCache(String currencyId) {
        long now = System.currentTimeMillis();
        long cacheTime = plugin.getConfigManager().getTopCacheMinutes() * 60 * 1000L;

        if (now - lastCacheUpdate.getOrDefault(currencyId, 0L) < cacheTime && topCache.containsKey(currencyId)) {
            return;
        }

        List<Map.Entry<UUID, Double>> topBalances = plugin.getDatabaseManager().getTopBalances(currencyId, 20);
        topCache.put(currencyId, topBalances);
        lastCacheUpdate.put(currencyId, now);
    }
}