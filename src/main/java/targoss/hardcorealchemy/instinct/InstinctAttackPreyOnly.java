/*
 * Copyright 2018 asanetargoss
 * 
 * This file is part of Hardcore Alchemy.
 * 
 * Hardcore Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.instinct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.instincts.ProviderInstinct;
import targoss.hardcorealchemy.network.MessageInstinctAttackPreyOnly;
import targoss.hardcorealchemy.network.MessageInstinctValue;
import targoss.hardcorealchemy.network.PacketHandler;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.util.MobLists;

/**
 * An instinct which makes the player unable to attack any entity
 * unless the player's form wants to attack it, or if the entity
 * is trying to hurt the player.
 * 
 * The effect wears off when the player kills enough of the desired
 * mobs.
 * 
 */
public class InstinctAttackPreyOnly implements IInstinct {
    
    public InstinctAttackPreyOnly() { }
    
    /** Make time (ticks) the player can not see prey while in a killing frenzy */
    private static final int MAX_TICKS_OUT_OF_SIGHT = 20 * 20;
    /** Max distance (Manhattan) that player will search for nearby creatures  */
    private static final int SIGHT_RANGE = 20;
    
    private static Set<Class<? extends EntityLivingBase>> HUMAN_CLASSES = null;
    private static Set<Class<? extends EntityLivingBase>> getHumanClasses() {
        if (HUMAN_CLASSES == null) {
            HUMAN_CLASSES = new HashSet<>();
            for (String human : MobLists.getHumans()) {
                Class<? extends EntityLivingBase> entityClass = (Class<? extends EntityLivingBase>)EntityList.NAME_TO_CLASS.get(human);
                if (entityClass != null) {
                    HUMAN_CLASSES.add(entityClass);
                }
            }
        }
        return HUMAN_CLASSES;
    }
    
    Random random = new Random();
    private int lastFrenzyCheckTick = 0;
    private EntityLivingBase trackedEntity = null;
    private Class<? extends EntityLivingBase> lastSeenPrey = null;
    
    private Class<? extends EntityLivingBase> ownEntityClass = null;
    /* For when the list of targets failed to initialize due to NBT serialization being called before a world could be loaded */
    private boolean targetsInitialized = false;
    private Set<Class<? extends EntityLivingBase>> targetEntityClasses = new HashSet<>();
    private int minRequiredKills = 1;
    
    public boolean active = false;
    public int numKills = 0;
    public int requiredKills = 0;
    public boolean inKillingFrenzy = false;
    /** Time since the player has seen prey while in a killing frenzy */
    private int timeSinceSeenPrey = 0;


    @Override
    public boolean doesMorphEntityHaveInstinct(EntityLivingBase morphEntity) {
        return EntityUtil.getAiTargetTasks((EntityLiving)morphEntity).size() > 0;
    }
    
    @Override
    public IInstinct createInstanceFromMorphEntity(EntityLivingBase morphEntity) {
        return new InstinctAttackPreyOnly((EntityLiving)morphEntity, 5);
    }
    
    /**
     * @param entityMorphedAs The entity from EntityMorph.getEntity()
     */
    public InstinctAttackPreyOnly(EntityLiving entityMorphedAs, int minRequiredKills) {
        this.ownEntityClass = entityMorphedAs.getClass();
        setDesiredTargets(entityMorphedAs);
        targetsInitialized = true;
        this.minRequiredKills = minRequiredKills;
    }
    
    public ITextComponent getRandomTargetName() {
        if (targetEntityClasses.size() == 0) {
            return null;
        }
        
        int indexToSelect = random.nextInt(targetEntityClasses.size());
        int i = 0;
        
        String chosenEntityName = null;
        Class<? extends EntityLivingBase> chosenEntityClass = null;
        for (Class<? extends EntityLivingBase> targetClass : targetEntityClasses) {
            if (i++ == indexToSelect) {
                chosenEntityClass = targetClass;
                break;
            }
        }
        
        return EntityUtil.getEntityName(chosenEntityClass);
    }
    
    @Override
    public ITextComponent getNeedMessage(EntityPlayer player) {
        ITextComponent targetName = getRandomTargetName();
        if (targetName == null) {
            HardcoreAlchemy.LOGGER.error("No known instinct prey. Entity class: " + ownEntityClass.getName());
            return null;
        }
        
        return new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.need", targetName);
    }
    
    @Override
    public ITextComponent getNeedMessageOnActivate(EntityPlayer player) {
        ITextComponent targetName = getRandomTargetName();
        if (targetName == null) {
            HardcoreAlchemy.LOGGER.error("No known instinct prey. Entity class: " + ownEntityClass.getName());
            return null;
        }
        
        return new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.activate", targetName);
    }
    
    //TODO: Create a fake world so we don't have to use this
    /**
     * Generally called by deserializeNBT(...) to figure out what "prey" the player's morph has.
     * Should only be called when a world is available.
     * Otherwise, we wait and call this later in getInactiveChangeOnTick(...).
     */
    private void initTargets() {
        if (targetsInitialized) {
            return;
        }
        EntityLiving testEntity = EntityUtil.createEntity((Class<? extends EntityLiving>)this.ownEntityClass);
        if (testEntity != null) {
            setDesiredTargets(testEntity);
        }
        targetsInitialized = true;
    }

    private void setDesiredTargets(EntityLiving entityMorphedAs) {
        targetEntityClasses.clear();
        for (EntityAITasks.EntityAITaskEntry aiTaskEntry : EntityUtil.getAiTargetTasks(entityMorphedAs)) {
            EntityAIBase task = aiTaskEntry.action;
            
            Class<? extends EntityLivingBase> targetClass = null;
            if (task instanceof EntityAIFindEntityNearestPlayer) {
                targetClass = EntityPlayer.class;
            }
            else if (task instanceof EntityAINearestAttackableTarget) {
                targetClass = ((EntityAINearestAttackableTarget)task).targetClass;
            }
            
            if (targetClass != null) {
                targetEntityClasses.add(targetClass);
                
                if (targetClass == EntityPlayer.class) {
                    /* Also add non-player humans from MobLists.getHumans()
                     * This allows more options for things to kill
                     */
                    for (Class<? extends EntityLivingBase> humanClass : getHumanClasses()) {
                        targetEntityClasses.add(humanClass);
                    }
                }
            }
        }
    }
    
    public static final String NBT_ENTITY_MORPHED_AS = "entityMorphedAs";
    public static final String NBT_MIN_REQUIRED_KILLS = "minRequiredKills";
    
    public static final String NBT_ACTIVE = "active";
    public static final String NBT_NUM_KILLS = "numKills";
    public static final String NBT_REQUIRED_KILLS = "requiredKills";
    public static final String NBT_IN_KILLING_FRENZY = "inKillingFrenzy";
    public static final String NBT_TIME_SINCE_SEEN_PREY = "timeSinceSeenPrey";
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        
        nbt.setString(NBT_ENTITY_MORPHED_AS, EntityList.getEntityStringFromClass(ownEntityClass));
        nbt.setInteger(NBT_MIN_REQUIRED_KILLS, minRequiredKills);
        
        nbt.setBoolean(NBT_ACTIVE, active);
        nbt.setInteger(NBT_NUM_KILLS, numKills);
        nbt.setInteger(NBT_REQUIRED_KILLS, requiredKills);
        nbt.setBoolean(NBT_IN_KILLING_FRENZY, inKillingFrenzy);
        nbt.setInteger(NBT_TIME_SINCE_SEEN_PREY, timeSinceSeenPrey);
        
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt == null) {
            return;
        }
        
        {
            String entityName = nbt.getString(NBT_ENTITY_MORPHED_AS);
            Class<? extends Entity> entityClass = EntityList.NAME_TO_CLASS.get(entityName);
            if (entityClass == null) {
                HardcoreAlchemy.LOGGER.error("Could not load attack instinct. No entity found for entity name '" + entityName + "'.");
                entityClass = EntityLiving.class;
            }
            else if (!EntityLiving.class.isAssignableFrom(entityClass)) {
                HardcoreAlchemy.LOGGER.error("Could not load attack instinct. Entity '" + entityName + "' does not have AI.");
                entityClass = EntityLiving.class;
            }
            else if (entityClass != ownEntityClass) {
                Class<? extends EntityLiving> entityLivingClass = (Class<? extends EntityLiving>)entityClass;
                this.ownEntityClass = entityLivingClass;
                if (MiscVanilla.getWorld() != null) {
                    initTargets();
                }
            }
        }
        
        minRequiredKills = nbt.getInteger(NBT_MIN_REQUIRED_KILLS);
        
        active = nbt.getBoolean(NBT_ACTIVE);
        numKills = nbt.getInteger(NBT_NUM_KILLS);
        requiredKills = nbt.getInteger(NBT_REQUIRED_KILLS);
        inKillingFrenzy = nbt.getBoolean(NBT_IN_KILLING_FRENZY);
        timeSinceSeenPrey = nbt.getInteger(NBT_TIME_SINCE_SEEN_PREY);
    }
    
    @Override
    public boolean shouldStayActive(EntityPlayer player) {
        return active;
    }

    @Override
    public void onActivate(EntityPlayer player) {
        active = true;
        numKills = 0;
        requiredKills = minRequiredKills;
        inKillingFrenzy = false;
    }

    @Override
    public void onDeactivate(EntityPlayer player) {
        active = false;
        numKills = 0;
        requiredKills = 0;
        inKillingFrenzy = false;
    }

    @Override
    public void tick(EntityPlayer player) {
        /* If the player is in a killing frenzy,
         * but the last entity falls out of their line of sight,
         * the player will want to kill even more of the entity
         */
        if (inKillingFrenzy) {
            if (!canIncreaseKillRequirement()) {
                inKillingFrenzy = false;
            }
            else {
                if (!hasSeenPreyRecently(player, null)) {
                    increaseKillRequirement();
                    if (player.world.isRemote) {
                        Chat.notifySP(player, new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.resist",
                                EntityUtil.getEntityName(lastSeenPrey)));
                    }
                    inKillingFrenzy = false;
                }
            }
        }
    }
    
    public boolean isTarget(EntityLivingBase entity) {
        for (Class targetClass : targetEntityClasses) {
            if (EntityUtil.isEntityLike(entity, targetClass)) {
                return true;
            }
        }
        return false;
    }
    
    private void syncRemote(EntityPlayerMP player) {
        PacketHandler.INSTANCE.sendTo(new MessageInstinctValue(player.getCapability(ProviderInstinct.INSTINCT_CAPABILITY, null)), player);
        PacketHandler.INSTANCE.sendTo(new MessageInstinctAttackPreyOnly(this), player);
    }

    @Override
    public boolean canAttack(EntityPlayer player, EntityLivingBase entity) {
        // Allow attacking if this entity wants to attack you
        if ((entity instanceof EntityLiving) && ((EntityLiving)entity).getAttackTarget() == player) {
            return true;
        }
        
        if (EntityUtil.isEntityLike(entity, ownEntityClass) && !targetEntityClasses.contains(entity.getClass())) {
            Chat.notify((EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.preserve_own_kind"));
            return false;
        }
        
        if (!isTarget(entity)) {
            if (targetEntityClasses.size() == 0) {
                HardcoreAlchemy.LOGGER.error("No known instinct prey. Entity class: " + ownEntityClass.getName());
                return true;
            }
            
            // Wrong target
            Chat.notify((EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.something_else", getRandomTargetName()));
            return false;
        }
        
        return true;
    }

    @Override
    public void afterKill(EntityPlayer player, EntityLivingBase entity) {
        if (!isTarget(entity)) {
            return;
        }
        
        // Desired prey has been killed
        numKills++;
        
        if (numKills >= requiredKills) {
            // Kill requirement satisfied
            inKillingFrenzy = false;
            Chat.notify((EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.enough_kills"));
            active = false;
            syncRemote((EntityPlayerMP)player);
            return;
        }
        
        if (inKillingFrenzy && !canIncreaseKillRequirement()) {
            inKillingFrenzy = false;
            syncRemote((EntityPlayerMP)player);
            return;
        }
        
        if (isPreyInLineOfSight(player, entity)) {
            // More prey visible nearby
            if (!inKillingFrenzy) {
                inKillingFrenzy = true;
                lastFrenzyCheckTick = player.ticksExisted;
                Chat.notify((EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.frenzy_start"));
                syncRemote((EntityPlayerMP)player);
            }
        }
        else {
            // Killed all prey in the area
            inKillingFrenzy = false;
            Chat.notify((EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.frenzy_end"));
            syncRemote((EntityPlayerMP)player);
        }
    }
    
    //TODO: Have a function getMaxRequiredKills() instead
    private boolean canIncreaseKillRequirement() {
        return requiredKills < minRequiredKills * 4;
    }
    
    private void increaseKillRequirement() {
        requiredKills++;
    }
    
    /**
     * Whether the one of the player's desired instinct prey
     * has recently been in their line of sight and nearby.
     * Valid only during a killing frenzy, as that is when
     * the timekeeping variable (lastFrenzyCheckTick) is reset.
     */
    private boolean hasSeenPreyRecently(EntityPlayer player, @Nullable EntityLivingBase excludedEntity) {
        boolean seenPrey = isPreyInLineOfSight(player, excludedEntity);

        // Check for integer overflow
        if (lastFrenzyCheckTick > player.ticksExisted) {
            lastFrenzyCheckTick = player.ticksExisted;
        }
        
        // Calculate change in time since the player last saw prey
        if (seenPrey) {
            timeSinceSeenPrey = 0;
        }
        else {
            timeSinceSeenPrey += player.ticksExisted - lastFrenzyCheckTick;
        }
        lastFrenzyCheckTick = player.ticksExisted;
        
        if (timeSinceSeenPrey < MAX_TICKS_OUT_OF_SIGHT) {
            return true;
        }
        return seenPrey;
    }
    
    /**
     * Whether the one of the player's desired instinct prey
     * is currently in their line of sight and nearby.
     */
    private boolean isPreyInLineOfSight(EntityPlayer player, @Nullable EntityLivingBase excludedEntity) {
        if (excludedEntity != null && excludedEntity == trackedEntity) {
            trackedEntity = null;
        }
        // Can we "see" the currently cached entity?
        if (trackedEntity != null && player.canEntityBeSeen(trackedEntity)) {
            return true;
        }
        else {
            trackedEntity = null;
            List<EntityLivingBase> availablePrey = new ArrayList<>();
            AxisAlignedBB aabb = new AxisAlignedBB(
                    player.posX-SIGHT_RANGE, player.posY-SIGHT_RANGE, player.posZ-SIGHT_RANGE,
                    player.posX+SIGHT_RANGE, player.posY+SIGHT_RANGE, player.posZ+SIGHT_RANGE
                    );
            for (Class<? extends EntityLivingBase> targetEntityClass : targetEntityClasses) {
                for (EntityLivingBase possiblePrey : EntityUtil.getEntitiesAndMorphsExcluding(player, player.world, targetEntityClass, aabb)) {
                    if (possiblePrey != excludedEntity && player.canEntityBeSeen(possiblePrey)) {
                        availablePrey.add(possiblePrey);
                    }
                }
            }
            
            if (availablePrey.size() <= 0) {
                return false;
            }
            
            // Choose entity at random
            trackedEntity = availablePrey.get(random.nextInt(availablePrey.size()));
            lastSeenPrey = trackedEntity.getClass();
        }
        
        return trackedEntity != null;
    }
    
    /**
     * Check if prey have been calculated yet.
     */
    @Override
    public float getInactiveChangeOnTick(EntityPlayer player) {
        if (!targetsInitialized && MiscVanilla.getWorld() != null) {
            initTargets();
        }
        return 0.0F;
    }

    @Override
    public float getInactiveChangeOnKill(EntityPlayer player, EntityLivingBase entity) {
        for (Class<? extends EntityLivingBase> targetClass : targetEntityClasses) {
            if (EntityUtil.isEntityLike(entity, targetClass)) {
                return 1.0F;
            }
        }
        return 0.0F;
    }
}
