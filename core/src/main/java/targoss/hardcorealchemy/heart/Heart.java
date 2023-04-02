/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.heart;

import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import targoss.hardcorealchemy.config.Configs;

public class Heart extends IForgeRegistryEntry.Impl<Heart> {
    public String name;
    public Item ITEM;
    public Item ITEM_SHARD;
    public ResourceLocation tileset;
    public int tileU;
    public int tileV;
    public int highlightTileU;
    public int highlightTileV;
    protected AttributeModifier modifier;
    protected static final String BASE_MODIFIER_NAME = "hardcorealchemy:default_hearts";
    protected static final UUID BASE_MODIFIER_ID = UUID.fromString("893bbc91-501b-4143-8aaa-640295b1e672");
    
    public Heart(ResourceLocation tileset, int tileU, int tileV, int highlightTileU, int highlightTileV) {
        this.tileset = tileset;
        this.tileU = tileU;
        this.tileV = tileV;
        this.highlightTileU = highlightTileU;
        this.highlightTileV = highlightTileV;
    }
    
    protected static final long seedBase1 = 8955423686119581968L;
    protected static final long seedBase2 = -5662688747627620733L;
    
    /**
     * Gets the modifier ID for the given heart ID, guaranteed constant
     * across sessions due to hardcoded seeds.
     * UUID has the following properties:
     * - First 16 bits are the same for all hearts
     * - Second 16 bits are the same for hearts from the same mod ID
     * - Remaining 32 bits are different for each heart
     */
    public static UUID getModifierIDForRegistryName(ResourceLocation id) {
        long seed1 = seedBase1 ^ (long)id.getResourceDomain().hashCode();
        Random random1 = new Random(seed1);
        long part1 = seedBase1 ^ (long)random1.nextInt();
        long seed2 = seedBase2 ^ ((long)id.getResourceDomain().hashCode() << 32) ^ (long)id.getResourcePath().hashCode();
        Random random2 = new Random(seed2);
        long part2 = random2.nextLong();
        UUID modifierID = new UUID(part1, part2);
        return modifierID;
    }
    
    protected static final double HEALTH_PER_HEART = 2.0D;
    protected static final int ADD = 0;
    
    /**
     * setRegistryName(...) must be called first
     */
    public AttributeModifier getModifier() {
        if (modifier == null) {
            ResourceLocation id = getRegistryName();
            UUID modifierID = getModifierIDForRegistryName(id);
            String modifierName = id.getResourceDomain() + ":heart_" + id.getResourcePath() + "_modifier";
            modifier = new AttributeModifier(modifierID, modifierName, HEALTH_PER_HEART, ADD);
        }
        return modifier;
    }
    
    public static AttributeModifier getBaseModifier(Configs configs, World world) {
        float healthOffset = 0.0f;
        if (configs.base.enableHearts) {
            switch (world.getDifficulty()) {
            case PEACEFUL:
                healthOffset = 0.0f;
                break;
            case EASY:
                healthOffset = -6.0f;
                break;
            case NORMAL:
                healthOffset = -10.0f;
                break;
            case HARD:
            default:
                healthOffset = -14.0f;
                break;
            }
        }
        return new AttributeModifier(BASE_MODIFIER_ID, BASE_MODIFIER_NAME, healthOffset, ADD);
    }
}
