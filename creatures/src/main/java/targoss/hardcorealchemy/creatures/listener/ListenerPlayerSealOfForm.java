/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.listener;

import static targoss.hardcorealchemy.item.Items.EMPTY_SLATE;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.MorphAbilityChangeReason;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.creatures.item.ItemSealOfForm;
import targoss.hardcorealchemy.creatures.item.Items;
import targoss.hardcorealchemy.creatures.util.MorphState;
import targoss.hardcorealchemy.event.EventEnchant;
import targoss.hardcorealchemy.item.ItemEmptySlate;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.Chat.Type;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.MobLists;

public class ListenerPlayerSealOfForm extends HardcoreAlchemyListener {
    {
        ItemEmptySlate.enchantmentEnabled = true;
    }
    
    protected Random random = new Random();
    
    @SubscribeEvent
    public void onEnchantSeal(EventEnchant.Post event) {
        if (InventoryUtil.isEmptyItemStack(event.enchantStack)) {
            return;
        }
        if (event.enchantStack.getItem() != EMPTY_SLATE) {
            return;
        }
        EntityPlayer player = event.player;
        ICapabilityHumanity humanity = player.getCapability(ProviderHumanity.HUMANITY_CAPABILITY, null);
        if (humanity != null && !humanity.shouldDisplayHumanity()) {
            Chat.message(Type.NOTIFY, (EntityPlayerMP)player, humanity.explainWhyCantMorph());
            return;
        }
        
        // Get the player's current form
        IMorphing morphing = Morphing.get(player);
        if (morphing == null) {
            return;
        }
        AbstractMorph sealMorph = morphing.getCurrentMorph();
        ItemStack sealOfFormStack = new ItemStack(Items.SEAL_OF_FORM);
        ItemSealOfForm.setMorphOnItem(sealOfFormStack, sealMorph);
        
        // Morph the player into some other form, depending on what they are morphed as and
        // what morphs are available.
        List<AbstractMorph> acquiredMorphs = morphing.getAcquiredMorphs();
        int availableFormCount = acquiredMorphs.size();
        if (humanity == null || !humanity.getHasForgottenHumanForm()) {
            ++availableFormCount;
        }

        // Do some calculation to check if we have a "fallback" morph, i.e. a morph to go back to
        // (or the player if available).
        // Here, it's important to handle an edge case where the player's current form is not in
        // the acquired morphs list. In that case, the player has an extra form to fall back to.
        final int sealMorphIndex = acquiredMorphs.indexOf(sealMorph);
        final boolean hasFallbackMorph = availableFormCount >= 2 || (availableFormCount >= 1 && !((sealMorph == null && !humanity.getHasForgottenHumanForm()) || sealMorphIndex != -1));
        AbstractMorph newMorph = null;
        if (!hasFallbackMorph) {
            // If no morphs are available, morph the player into a slime.
            EntitySlime slime = new EntitySlime(player.world);
            slime.setSlimeSize(0);
            newMorph = MorphState.createMorph(slime);
        }
        else if (sealMorph == null || humanity.getHasForgottenHumanForm()) {
            // Check the player's morphing history. Morph the player into their most recent
            // morph if possible. This reduces unexpected consequences of morphing.
            // Use a try-catch because the upstream Metamorph may not have this function.
            AbstractMorph lastSelectedMorph;
            try {
                lastSelectedMorph = morphing.getLastSelectedMorph();
            } catch (Exception e) {
                lastSelectedMorph = null;
            }
            if (lastSelectedMorph != null && !lastSelectedMorph.equals(sealMorph) && acquiredMorphs.contains(lastSelectedMorph)) {
                newMorph = lastSelectedMorph;
            }
            if (newMorph == null) {
                int offset = sealMorphIndex;
                int n = acquiredMorphs.size();
                int range = n;
                if (offset != -1) {
                    --range;
                    ++offset;
                }
                else {
                    offset = 0;
                }
                int randomMorphIndexExcludingCurrent = (random.nextInt(range) + offset) % n;
                newMorph = acquiredMorphs.get(randomMorphIndexExcludingCurrent);
            }
        }
        else {
            newMorph = null;
        }
        
        if (availableFormCount > 0) {
            MorphState.forceForm(coreConfigs, player, MorphAbilityChangeReason.FORGOT_LAST_FORM, newMorph);
        }
        
        // Bind the player's form to the seal
        event.enchantStack = sealOfFormStack;
        
        if (availableFormCount == 0) {
            // What exactly did you expect to happen?
            player.attackEntityFrom(DamageSource.outOfWorld, 1000.0F);
        }
    }
    
    protected static class SealLootFunction extends LootFunction {
        protected List<String> mobs = new ArrayList<>();
        protected Random rand = new Random();

        public SealLootFunction(String... mobs) {
            super(new LootCondition[] {});
            assert(mobs.length > 0);
            for (String mob : mobs) {
                this.mobs.add(mob);
            }
        }

        public SealLootFunction(LootCondition[] conditions, List<String> mobs) {
            super(conditions);
            assert(mobs.size() > 0);
            for (String mob : mobs) {
                this.mobs.add(mob);
            }
        }

        @Override
        public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
            stack = stack.copy();

            while (!mobs.isEmpty()) {
                int n = mobs.size();
                int i = rand.nextInt(n);
                String mob = mobs.get(i);
                if (EntityList.isStringValidEntityName(mob)) {
                    ItemSealOfForm.setEntityMorphOnItem(stack, mob, null);
                    break;
                }
                else {
                    mobs.remove(i);
                }
            }
            if (mobs.isEmpty()) {
                ItemSealOfForm.setEntityMorphOnItem(stack, MobLists.PIG, null);
            }
            
            return stack;
        }
        
        public static class Serializer extends LootFunction.Serializer<SealLootFunction> {

            protected Serializer(ResourceLocation location, Class<SealLootFunction> clazz) {
                super(new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "random_morph_seal"), SealLootFunction.class);
            }

            @Override
            public void serialize(JsonObject object, SealLootFunction functionClazz,
                    JsonSerializationContext serializationContext) {
                if (functionClazz.mobs != null && !functionClazz.mobs.isEmpty())
                {
                    JsonArray jsonarray = new JsonArray();

                    for (String mob : functionClazz.mobs)
                    {
                        jsonarray.add(new JsonPrimitive(mob));
                    }

                    object.add("morphs", jsonarray);
                }
                
            }

            @Override
            public SealLootFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
                    LootCondition[] conditionsIn) {
                List<String> mobs = new ArrayList<String>();

                if (object.has("morphs"))
                {
                    for (JsonElement jsonelement : JsonUtils.getJsonArray(object, "morphs"))
                    {
                        String mob = JsonUtils.getString(jsonelement, "morph");
                        mobs.add(mob);
                    }
                }

                return new SealLootFunction(conditionsIn, mobs);
            }
            
        }
    }
    
    protected static LootPool buildSealLootPool(String name, String... mobs) {
        LootEntryItem entry = new LootEntryItem(Items.SEAL_OF_FORM,
                1,
                0,
                new LootFunction[] { new SealLootFunction(mobs) },
                new LootCondition[]{ new RandomChance(0.2F) },
                HardcoreAlchemyCore.MOD_ID + ":seal_of_form_loot_" + name);
        LootPool pool = new LootPool(new LootEntry[] { entry },
                new LootCondition[]{},
                new RandomValueRange(1, 2),
                new RandomValueRange(0),
                HardcoreAlchemyCore.MOD_ID + ":seal_of_form_pool_" + name);
        return pool;
    }
    
    @SubscribeEvent
    public void onLoot(LootTableLoadEvent event) {
        String name = event.getName().toString();
        if (name.equals("minecraft:chests/abandoned_mineshaft")) {
            event.getTable().addPool(buildSealLootPool("mineshaft", MobLists.CAVE_SPIDER, MobLists.SPIDER));
        }
        else if (name.equals("minecraft:chests/stronghold_corridor")) {
            event.getTable().addPool(buildSealLootPool("stronghold", MobLists.SILVERFISH, MobLists.SKELETON, MobLists.ZOMBIE, MobLists.ENDERMAN));
        }
        else if (name.equals("minecraft:chests/igloo_chest")) {
            event.getTable().addPool(buildSealLootPool("igloo", MobLists.ZOMBIE, MobLists.POLAR_BEAR, MobLists.WOLF));
        }
        else if (name.equals("minecraft:chests/jungle_temple")) {
            event.getTable().addPool(buildSealLootPool("jungle_temple", MobLists.OCELOT, MobLists.CREEPER));
        }
    }
}
