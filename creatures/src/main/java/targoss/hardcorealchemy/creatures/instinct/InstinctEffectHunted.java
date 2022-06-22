/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.creatures.instinct;

import java.util.Random;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.capability.entitystate.ICapabilityEntityState;
import targoss.hardcorealchemy.capability.entitystate.ProviderEntityState;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.creatures.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.RandomUtil;

/**
 * The player will be visited by a wolf that wants to hunt them.
 * If they are a wolf, they will be visited by a bear instead.
 * The predator becomes increasingly powerful as the amplifier goes up.
 */
public class InstinctEffectHunted extends InstinctEffect {
    @CapabilityInject(ICapabilityInstinct.class)
    private static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    
    @CapabilityInject(IMorphing.class)
    private static final Capability<IMorphing> MORPHING_CAPABILITY = null;
    
    private Random random = new Random();
    
    /** Amplifier for which the potion amplifier is 1. Higher levels will be extrapolated. */
    public static final float POTION_AMPLIFIER_1_THRESHOLD = 2.5F;
    
    protected int getPotionEffectStrength(float amplifier) {
        return (int)(0.5F + (0.5F * amplifier / POTION_AMPLIFIER_1_THRESHOLD));
    }
    
    public static enum EventType {
        NONE(-1.0F),
        WARNING_MESSAGE(0.0F),
        PREDATOR_SOUND(1.0F),
        PREDATOR_APPEARS(2.0F);
        final float threshold;
        EventType(float threshold) {
            this.threshold = threshold;
            // Maybe add a different timer for each type
        }
    }
    
    public static final int EVENT_TIME_MIN = 3 * 60 * 20;
    public static final int EVENT_TIME_MAX = 6 * 60 * 20;
    
    protected static class Data implements IInstinctEffectData {
        public EventType eventType = EventType.NONE;
        public int timer = 0;
        
        public static final String NBT_EVENT_TYPE = "event_type";
        public static final String NBT_TIMER = "timer";
        
        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            
            nbt.setByte(NBT_EVENT_TYPE, (byte)eventType.ordinal());
            nbt.setInteger(NBT_TIMER, timer);
            
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            byte eventTypeNum = nbt.getByte(NBT_EVENT_TYPE);
            if (eventTypeNum >= 0 && eventTypeNum < EventType.values().length) {
                eventType = EventType.values()[eventTypeNum];
            }
            timer = nbt.getInteger(NBT_TIMER);
        }
    }
    
    @Override
    public IInstinctEffectData createData() {
        return new Data();
    }
    
    @Override
    public void onActivate(EntityPlayer player, float amplifier) {}

    @Override
    public void onDeactivate(EntityPlayer player, float amplifier) {
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        Data data = (Data)instinct.getInstinctEffectData(this);
        data.eventType = EventType.NONE;
    }
    
    protected SoundEvent getPredatorSound(EntityPlayer player) {
        SoundEvent defaultSound = SoundEvents.ENTITY_WOLF_GROWL;
        
        IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
        if (morphing == null) {
            return defaultSound;
        }
        
        AbstractMorph morph = morphing.getCurrentMorph();
        if (morph == null || !(morph instanceof EntityMorph)) {
            return defaultSound;
        }
        EntityLivingBase morphEntity = ((EntityMorph)morph).getEntity(player.world);
        if (morphEntity instanceof EntityWolf) {
            return SoundEvents.ENTITY_POLAR_BEAR_WARNING;
        }
        else {
            return defaultSound;
        }
    }
    
    protected EntityLivingBase getPredator(EntityPlayer player) {
        IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
        if (morphing == null) {
            return new EntityWolf(player.world);
        }
        
        AbstractMorph morph = morphing.getCurrentMorph();
        if (morph == null || !(morph instanceof EntityMorph)) {
            return new EntityWolf(player.world);
        }
        EntityLivingBase morphEntity = ((EntityMorph)morph).getEntity(player.world);
        if (morphEntity instanceof EntityWolf) {
            return new EntityPolarBear(player.world);
        }
        else {
            return new EntityWolf(player.world);
        }
    }

    @Override
    public void tick(EntityPlayer player, float amplifier) {
        if (player.world.isRemote) {
            return;
        }
        
        if (amplifier < EventType.WARNING_MESSAGE.threshold) {
            return;
        }
        
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        Data data = (Data)instinct.getInstinctEffectData(this);
        
        if (data.timer <= 0) {
            if (data.eventType != EventType.NONE) {
                switch (data.eventType) {
                case WARNING_MESSAGE:
                    Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.instinct.effect.hunted.warn.variant1"));
                    break;
                case PREDATOR_SOUND:
                    float soundX = (float)player.posX + RandomUtil.getRandomInRangeSigned(random, 5.0F, 10.0F);
                    float soundY = (float)player.posY + RandomUtil.getRandomInRangeSigned(random, 5.0F, 10.0F);
                    float soundZ = (float)player.posZ + RandomUtil.getRandomInRangeSigned(random, 5.0F, 10.0F);
                    SoundEvent predatorSound = getPredatorSound(player);
                    float pitch = (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F; 
                    SPacketSoundEffect predatorSoundPacket = new SPacketSoundEffect(predatorSound, SoundCategory.NEUTRAL, soundX, soundY, soundZ, 1.0F, pitch);
                    ((EntityPlayerMP)player).connection.sendPacket(predatorSoundPacket);
                    break;
                case PREDATOR_APPEARS:
                    EntityLivingBase predator = getPredator(player);
                    
                    BlockPos predatorPos = RandomUtil.findSuitableBlockPosInRangeSigned(
                        random, 14,
                        player.getPosition(), 5.0F, 10.0F,
                        (BlockPos pos) -> {
                            predator.setPosition(pos.getX(), pos.getY(), pos.getZ());
                            if (predator.isEntityInsideOpaqueBlock()) {
                                return false;
                            }
                            predator.setPosition(pos.getX(), pos.getY() - 1, pos.getZ());
                            if (!predator.isEntityInsideOpaqueBlock()) {
                                return false;
                            }
                            return true;
                        }
                    );
                    if (predatorPos != null) {
                        predator.setPosition(predatorPos.getX(), predatorPos.getY(), predatorPos.getZ());
                        
                        ICapabilityEntityState entityState = predator.getCapability(ProviderEntityState.CAPABILITY, null);
                        if (entityState != null) {
                            ICapabilityMisc misc = player.getCapability(ProviderMisc.MISC_CAPABILITY, null);
                            if (misc != null) {
                                // Make the player the predator's highest priority target
                                entityState.setTargetPlayerID(misc.getLifetimeUUID());
                            }
                            // Prevent the entity from existing forever, to prevent lag
                            entityState.setLifetime(EVENT_TIME_MAX);
                        }
                        
                        // Predator will now hunt the player
                        // Apply potion effects to make the predator stronger, depending on the amplifier
                        int potionStrength = getPotionEffectStrength(amplifier);
                        predator.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, Integer.MAX_VALUE, potionStrength));
                        predator.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, Integer.MAX_VALUE, potionStrength));
                        predator.world.spawnEntity(predator);
                    }
                    break;
                default:
                    break;
                }
            }
            
            // Choose next random event and reset countdown.
            // Some random chance of weaker event, but never an event stronger than amplifier
            float rand = random.nextFloat();
            float newEvent;
            if (rand == 0.0F) {
                newEvent = amplifier;
            }
            else {
                newEvent = amplifier * 0.5F / rand;
                if (newEvent > amplifier) {
                    newEvent = amplifier;
                }
            }
            EventType[] eventTypes = EventType.values();
            for (int i = eventTypes.length - 1; i >= 0; i--) {
                if (newEvent >= eventTypes[i].threshold) {
                    data.eventType = eventTypes[i];
                    data.timer = EVENT_TIME_MIN + random.nextInt(1 + EVENT_TIME_MAX - EVENT_TIME_MIN);
                    break;
                }
            }
        }
        
        data.timer--;
    }
    
    @Override
    public boolean canAttack(EntityPlayer player, float amplifier, EntityLivingBase entity) {
        if (amplifier < EventType.PREDATOR_APPEARS.threshold) {
            return true;
        }
        
        ICapabilityEntityState entityState = entity.getCapability(ProviderEntityState.CAPABILITY, null);
        if (entityState == null) {
            return true;
        }
        
        ICapabilityMisc misc = player.getCapability(ProviderMisc.MISC_CAPABILITY, null);
        if (misc == null) {
            return true;
        }
        
        if (entityState.getTargetPlayerID().equals(misc.getLifetimeUUID())) {
            Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.instinct.effect.hunted.cannot_attack", EntityUtil.getEntityName(entity)), 20);
            return false;
        } else {
            return true;
        }
    }
    
}
