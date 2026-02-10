package com.busybee.obfeco.util;

import com.busybee.obfeco.Obfeco;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class SignInput implements Listener {
    private final Obfeco plugin;
    private final Map<UUID, Consumer<String[]>> listeners = new HashMap<>();
    private final Map<UUID, BlockData> originalBlocks = new HashMap<>();
    private final Map<UUID, Location> signLocations = new HashMap<>();

    public SignInput(Obfeco plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player, String[] lines, Consumer<String[]> callback) {
        UUID uuid = player.getUniqueId();
        
        // Cleanup any previous sign if it exists
        cleanup(uuid);
        
        listeners.put(uuid, callback);

        // Use a location slightly above the player
        Location loc = player.getLocation().clone();
        loc.setY(Math.min(loc.getWorld().getMaxHeight() - 1, loc.getY() + 2));
        
        Block block = loc.getBlock();
        originalBlocks.put(uuid, block.getBlockData());
        signLocations.put(uuid, loc);
        
        // Set the block to a sign in the world
        block.setType(Material.OAK_SIGN, false);
        Sign sign = (Sign) block.getState();
        
        for (int i = 0; i < Math.min(lines.length, 4); i++) {
            sign.setLine(i, lines[i]);
        }
        sign.update(true, false);

        // Delay opening the sign by 2 ticks to ensure client sync
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.openSign(sign);
            }
        }, 2L);
        
        // Safety timeout to revert block if player disconnects or something goes wrong
        Bukkit.getScheduler().runTaskLater(plugin, () -> cleanup(uuid), 600L); // 30 seconds
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Consumer<String[]> callback = listeners.remove(uuid);
        
        if (callback != null) {
            callback.accept(event.getLines());
            event.setCancelled(true);
            cleanup(uuid);
        }
    }
    
    private void cleanup(UUID uuid) {
        Location loc = signLocations.remove(uuid);
        BlockData oldData = originalBlocks.remove(uuid);
        listeners.remove(uuid);
        
        if (loc != null && oldData != null) {
            loc.getBlock().setBlockData(oldData, false);
        }
    }
}