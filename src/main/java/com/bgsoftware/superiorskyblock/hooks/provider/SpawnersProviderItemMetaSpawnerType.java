package com.bgsoftware.superiorskyblock.hooks.provider;

import com.google.common.base.Preconditions;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public interface SpawnersProviderItemMetaSpawnerType extends SpawnersProvider_AutoDetect {

    default String getSpawnerType(ItemStack itemStack){
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");

        if(itemStack.getItemMeta() instanceof BlockStateMeta){
            CreatureSpawner creatureSpawner = (CreatureSpawner) ((BlockStateMeta) itemStack.getItemMeta()).getBlockState();
            return creatureSpawner.getSpawnedType().name();
        }

        return "PIG";
    }

}
