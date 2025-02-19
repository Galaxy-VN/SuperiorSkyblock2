package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.hooks.StackedBlocksSnapshotProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.hooks.support.WildStackerSnapshotsContainer;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.key.ConstantKeys;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceEvent;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.events.BarrelStackEvent;
import com.bgsoftware.wildstacker.api.events.BarrelUnstackEvent;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
import com.bgsoftware.wildstacker.api.objects.StackedSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.stream.Collectors;

public final class StackedBlocksProvider_WildStacker implements StackedBlocksProvider_AutoDetect, StackedBlocksSnapshotProvider {

    private static boolean registered = false;

    public StackedBlocksProvider_WildStacker(){
        if(!registered) {
            Bukkit.getPluginManager().registerEvents(new StackerListener(), SuperiorSkyblockPlugin.getPlugin());
            registered = true;

            SuperiorSkyblockAPI.getBlockValues().registerKeyParser(new CustomKeyParser() {

                private final SystemManager systemManager = WildStackerAPI.getWildStacker().getSystemManager();

                @Override
                public com.bgsoftware.superiorskyblock.api.key.Key getCustomKey(Location location) {
                    return systemManager.isStackedBarrel(location) ?
                            com.bgsoftware.superiorskyblock.api.key.Key.of(systemManager.getStackedBarrel(location).getBarrelItem(1)) :
                            com.bgsoftware.superiorskyblock.api.key.Key.of("CAULDRON");
                }

                @Override
                public boolean isCustomKey(com.bgsoftware.superiorskyblock.api.key.Key key) {
                    return false;
                }

            }, ConstantKeys.CAULDRON);

            SuperiorSkyblockPlugin.log("Using WildStacker as a stacked-blocks provider.");
        }
    }

    @Override
    public Collection<Pair<Key, Integer>> getBlocks(World world, int chunkX, int chunkZ) {
        StackedSnapshot stackedSnapshot = WildStackerSnapshotsContainer.getSnapshot( ChunkPosition.of(world, chunkX, chunkZ));

        try {
            return stackedSnapshot.getAllBarrelsItems().values().stream()
                    .filter(entry -> entry.getValue() != null)
                    .map(entry -> new Pair<>(Key.of(entry.getValue()), entry.getKey()))
                    .collect(Collectors.toSet());
        }catch (Throwable ex){
            return stackedSnapshot.getAllBarrels().values().stream()
                    .map(entry -> new Pair<>(Key.of(entry.getValue(), (short) 0), entry.getKey()))
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void takeSnapshot(Chunk chunk) {
        WildStackerSnapshotsContainer.takeSnapshot(chunk);
    }

    @Override
    public void releaseSnapshot(World world, int chunkX, int chunkZ) {
        WildStackerSnapshotsContainer.releaseSnapshot(ChunkPosition.of(world, chunkX, chunkZ));
    }

    @SuppressWarnings("unused")
    private static class StackerListener implements Listener {

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelPlace(BarrelPlaceEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());

            if(island == null)
                return;

            com.bgsoftware.superiorskyblock.key.Key blockKey = com.bgsoftware.superiorskyblock.key.Key.of(e.getBarrel().getBarrelItem(1));
            int increaseAmount = e.getBarrel().getStackAmount();

            if(island.hasReachedBlockLimit(blockKey, increaseAmount)){
                e.setCancelled(true);
                Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(blockKey.toString()));
            }

            else{
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelStack(BarrelStackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());

            if(island == null)
                return;

            com.bgsoftware.superiorskyblock.key.Key blockKey = com.bgsoftware.superiorskyblock.key.Key.of(e.getTarget().getBarrelItem(1));
            int increaseAmount = e.getTarget().getStackAmount();

            if(island.hasReachedBlockLimit(blockKey, increaseAmount)){
                e.setCancelled(true);
            }

            else{
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelUnstack(BarrelUnstackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
            if(island != null)
                island.handleBlockBreak(com.bgsoftware.superiorskyblock.key.Key.of(e.getBarrel().getBarrelItem(1)), e.getAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelPlace(BarrelPlaceInventoryEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());

            if(island == null)
                return;

            com.bgsoftware.superiorskyblock.key.Key blockKey = com.bgsoftware.superiorskyblock.key.Key.of(e.getBarrel().getBarrelItem(1));
            int increaseAmount = e.getIncreaseAmount();

            if(island.hasReachedBlockLimit(blockKey, increaseAmount)){
                e.setCancelled(true);
                Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(blockKey.toString()));
            }

            else{
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

    }

}
