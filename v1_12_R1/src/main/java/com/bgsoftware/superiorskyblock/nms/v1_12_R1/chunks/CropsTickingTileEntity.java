package com.bgsoftware.superiorskyblock.nms.v1_12_R1.chunks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.ITickable;
import net.minecraft.server.v1_12_R1.TileEntity;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class CropsTickingTileEntity extends TileEntity implements ITickable {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Map<Long, CropsTickingTileEntity> tickingChunks = new HashMap<>();
    private static int random = ThreadLocalRandom.current().nextInt();

    private final WeakReference<Island> island;
    private final WeakReference<Chunk> chunk;
    private final int chunkX, chunkZ;

    private int currentTick = 0;

    public static void create(Island island, Chunk chunk) {
        long chunkKey = ChunkCoordIntPair.a(chunk.locX, chunk.locZ);
        if (!tickingChunks.containsKey(chunkKey)) {
            tickingChunks.put(chunkKey, new CropsTickingTileEntity(island, chunk));
        }
    }

    public static CropsTickingTileEntity remove(ChunkCoordIntPair chunkCoords) {
        return tickingChunks.remove(ChunkCoordIntPair.a(chunkCoords.x, chunkCoords.z));
    }

    private CropsTickingTileEntity(Island island, Chunk chunk) {
        this.island = new WeakReference<>(island);
        this.chunk = new WeakReference<>(chunk);
        this.chunkX = chunk.locX;
        this.chunkZ = chunk.locZ;
        a(chunk.getWorld());
        setPosition(new BlockPosition(chunkX << 4, 1, chunkZ << 4));
        world.tileEntityListTick.add(this);
    }

    @Override
    public void e() {
        if (++currentTick <= plugin.getSettings().getCropsInterval())
            return;

        Chunk chunk = this.chunk.get();
        Island island = this.island.get();

        if (chunk == null || island == null) {
            world.tileEntityListTick.remove(this);
            return;
        }

        currentTick = 0;

        int worldRandomTick = world.getGameRules().c("randomTickSpeed");
        double cropGrowth = island.getCropGrowthMultiplier() - 1;

        int chunkRandomTickSpeed = (int) (worldRandomTick * cropGrowth * plugin.getSettings().getCropsInterval());

        if (chunkRandomTickSpeed > 0) {
            for (ChunkSection chunkSection : chunk.getSections()) {
                if (chunkSection != Chunk.a && chunkSection.shouldTick()) {
                    for (int i = 0; i < chunkRandomTickSpeed; i++) {
                        random = random * 3 + 1013904223;
                        int factor = random >> 2;
                        int x = factor & 15;
                        int z = factor >> 8 & 15;
                        int y = factor >> 16 & 15;
                        IBlockData blockData = chunkSection.getType(x, y, z);
                        Block block = blockData.getBlock();
                        if (block.isTicking() && plugin.getSettings().getCropsToGrow().contains(CraftMagicNumbers.getMaterial(block).name())) {
                            block.a(world, new BlockPosition(x + (chunkX << 4), y + chunkSection.getYPosition(), z + (chunkZ << 4)),
                                    blockData, ThreadLocalRandom.current());
                        }
                    }
                }
            }
        }

    }

}
