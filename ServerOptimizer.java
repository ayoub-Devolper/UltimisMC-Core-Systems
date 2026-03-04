package net.ultimismc.core.optimizer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerOptimizer extends JavaPlugin {
    @Override
    public void onEnable() {
        // Runs every 5 minutes (6000 ticks)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof Item) {
                        entity.remove();
                    }
                }
            }
            // Calling Garbage Collector to free up System RAM
            System.gc();
            Bukkit.getLogger().info("[UltimisOptimizer] Entities cleared and RAM optimized.");
        }, 6000L, 6000L);
    }
}
