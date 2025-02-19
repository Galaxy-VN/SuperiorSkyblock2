package com.bgsoftware.superiorskyblock.nms.v1_12_R1.algorithms;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;

public final class GlowEnchantment extends Enchantment {

    public static GlowEnchantment createEnchantment(){
        int id = 100;

        //noinspection deprecation, StatementWithEmptyBody
        while (Enchantment.getById(++id) != null) {
        }

        return new GlowEnchantment(id);
    }

    private GlowEnchantment(int id){
        super(id);
    }

    @Override
    public String getName() {
        return "SuperiorSkyblockGlow";
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override
    public boolean conflictsWith(Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean canEnchantItem(org.bukkit.inventory.ItemStack itemStack) {
        return true;
    }

    public boolean isTreasure() {
        return false;
    }

    public boolean isCursed() {
        return false;
    }

}
