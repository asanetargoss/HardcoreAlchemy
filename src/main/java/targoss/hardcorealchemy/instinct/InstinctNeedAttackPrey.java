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

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
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
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.instinct.api.IInstinctState;
import targoss.hardcorealchemy.instinct.api.InstinctState;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.util.MobLists;

/**
 * An instinct need for the player to attack any creatures
 * which the corresponding mob would attack.
 */
public class InstinctNeedAttackPrey implements IInstinctNeed {
    
    public InstinctNeedAttackPrey() { }
    
    /** Make time (ticks) the player can not see prey they have coveted (ie have seen) */
    private static final int MAX_TICKS_OUT_OF_SIGHT = 20 * 20;
    /** Max distance (per axis) that player will search for nearby creatures  */
    private static final int SIGHT_RANGE = 20;
    /** How long the player will continue to "crave" the same type of prey in the need messages after they have seen it */
    private static final int PREY_YEARN_MEMORY_TIME = 15 * 60 * 20;
    /** How long instinct will decrease if the player kills an undesired entity */
    private static final int SUDDEN_URGE_TIME = 7 * 20;
    
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
    
    /** Indicates that trackedEntity was non-null and the entity has not died */
    private boolean covetsPrey = false;
    /** Time since the player has seen prey (in ticks) */
    private int timeSinceSeenPrey = 0;
    /** Client and server don't always agree on this value, but if the player sees A target, it will show up here */
    private Class<? extends EntityLivingBase> lastSeenPrey = null;
    /**
     * Whether the player has killed their first prey.
     * Once this happens, the player will feel a strong urge to kill prey again,
     * any time they kill another entity that is not their prey.
     * */
    private boolean hasKilled = false;
    /**
     * If greater than zero, will cause instinct to decrease quickly and will count down each tick.
     * If prey is sensed, this is reset to zero.
     * */
    private int suddenUrgeTimer = 0;
    
    Random random = new Random();
    private int lastPreySightTick = 0;
    /** Whether prey was seen in the previous tick */
    private boolean sawPrey = false;
    private EntityLivingBase trackedEntity = null;
    
    private Class<? extends EntityLivingBase> ownEntityClass = null;
    /** Whether initTargets() was called */
    private boolean targetsInitialized = false;
    private Set<Class<? extends EntityLivingBase>> targetEntityClasses = new HashSet<>();
    
    @Override
    public IInstinctNeed createInstanceFromMorphEntity(EntityLivingBase morphEntity) {
        if (!(morphEntity instanceof EntityLiving)) {
            throw new UnsupportedOperationException("Cannot create an instance of " + InstinctNeedAttackPrey.class.getName() + " with a morph that is not EntityLiving");
        }
        return new InstinctNeedAttackPrey((EntityLiving)morphEntity);
    }
    
    /**
     * @param entityMorphedAs The entity from EntityMorph.getEntity()
     */
    public InstinctNeedAttackPrey(EntityLiving entityMorphedAs) {
        this.ownEntityClass = entityMorphedAs.getClass();
        setDesiredTargets(entityMorphedAs);
        targetsInitialized = true;
    }
    
    private ITextComponent getRandomTargetName() {
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
    
    private ITextComponent getYearnedTargetName() {
        if (lastSeenPrey != null && timeSinceSeenPrey < PREY_YEARN_MEMORY_TIME) {
            return EntityUtil.getEntityName(lastSeenPrey);
        } else {
            return getRandomTargetName();
        }
    }
    
    @Override
    public ITextComponent getNeedMessage(InstinctState.NeedStatus needStatus) {
        if (needStatus != InstinctState.NeedStatus.NONE) {
            if (trackedEntity != null) {
                ITextComponent trackedEntityName = EntityUtil.getEntityName(trackedEntity.getClass());
                // You resist the urge to kill X, but the urge grows stronger
                return new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.resist", trackedEntityName);
                // TODO: Implement "even though you don't see the X, you know it is still there")
            }
            return null;
        }
        
        ITextComponent targetName = getYearnedTargetName();
        if (targetName == null) {
            HardcoreAlchemy.LOGGER.error("No known instinct prey. Entity class: " + ownEntityClass.getName());
            return null;
        }
        
        return new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.need", targetName);
    }
    
    @Override
    public ITextComponent getNeedUnfulfilledMessage(InstinctState.NeedStatus needStatus) {
        return null;
    }
    
    //TODO: Create a fake world so we can do this earlier
    /**
     * Called to figure out what "prey" the player's morph has.
     * Should only be called when a world is available.
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
    
    private static final String NBT_COVETS_PREY = "covetsPrey";
    private static final String NBT_TIME_SINCE_SEEN_PREY = "timeSinceSeenPrey";
    private static final String NBT_LAST_SEEN_PREY = "lastSeenPrey";
    private static final String NBT_HAS_KILLED = "hasKilled";
    private static final String NBT_SUDDEN_URGE_TIMER = "suddenUrgeTimer";
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setBoolean(NBT_COVETS_PREY, covetsPrey);
        nbt.setInteger(NBT_TIME_SINCE_SEEN_PREY, timeSinceSeenPrey);
        if (lastSeenPrey != null) {
            nbt.setString(NBT_LAST_SEEN_PREY, EntityList.getEntityStringFromClass(lastSeenPrey));
        }
        nbt.setBoolean(NBT_HAS_KILLED, hasKilled);
        nbt.setInteger(NBT_SUDDEN_URGE_TIMER, suddenUrgeTimer);
        
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        covetsPrey = nbt.getBoolean(NBT_COVETS_PREY);
        timeSinceSeenPrey = nbt.getInteger(NBT_TIME_SINCE_SEEN_PREY);
        if (nbt.hasKey(NBT_LAST_SEEN_PREY)) {
            Class<? extends Entity> possibleLastSeenPrey = EntityList.getClassFromID(EntityList.getIDFromString(nbt.getString(NBT_LAST_SEEN_PREY)));
            if (EntityLivingBase.class.isAssignableFrom(possibleLastSeenPrey)) {
                lastSeenPrey = (Class<? extends EntityLivingBase>)possibleLastSeenPrey;
            }
        }
        hasKilled = nbt.getBoolean(NBT_HAS_KILLED);
        suddenUrgeTimer = nbt.getInteger(NBT_SUDDEN_URGE_TIMER);
    }
    
    private static int DETECT_MESSAGE_COOLDOWN = 10 * 20;
    private static String DETECT_MESSAGE_KEY = "detect_prey";
    private static int DETECT_DEAD_MESSAGE_COOLDOWN = 20 * 20;
    private static String DETECT_DEAD_MESSAGE_KEY = "detect_prey_dead";

    @Override
    public void tick(IInstinctState instinctState) {
        EntityPlayer player = instinctState.getPlayer();
        
        if (!targetsInitialized && MiscVanilla.getWorld() != null) {
            initTargets();
        }
        
        boolean covetedPrey = covetsPrey;
        EntityLivingBase lastTrackedEntity = trackedEntity;
        updateTrackedEntity(player);
        boolean hasSeenPrey = hasSeenPreyRecently(player);
        
        if (trackedEntity != null) {
            suddenUrgeTimer = 0;
            instinctState.setNeedStatus(IInstinctState.NeedStatus.URGENT);
            if (lastTrackedEntity == null ||
                    (lastTrackedEntity != trackedEntity && !isKilled(lastTrackedEntity))) {
                if (player.world.isRemote) {
                    Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.detect",
                        EntityUtil.getEntityName(lastSeenPrey)), DETECT_MESSAGE_COOLDOWN, DETECT_MESSAGE_KEY);
                }
            }
        }
        else {
            if (!hasSeenPrey && covetsPrey) {
                instinctState.setNeedStatus(IInstinctState.NeedStatus.WAITING);
                if (sawPrey) {
                    // The last prey fell out of their line of sight, but has not died
                    if (player.world.isRemote) {
                        Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.resist",
                                EntityUtil.getEntityName(lastSeenPrey)));
                    }
                }
                if (suddenUrgeTimer > 0) {
                    instinctState.setNeedStatus(InstinctState.NeedStatus.URGENT);
                    suddenUrgeTimer--;
                }
            }
        }
        
        sawPrey = hasSeenPrey;
    }
    
    @Override
    public void afterKill(IInstinctState instinctState, EntityLivingBase entity) {
        EntityPlayer player = instinctState.getPlayer();
        if (isTarget(entity)) {
            hasKilled = true;
            updateTrackedEntity(player);
            if (trackedEntity != null) {
                instinctState.setNeedStatus(IInstinctState.NeedStatus.URGENT);
                Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.kill_another",
                        EntityUtil.getEntityName(entity.getClass())), DETECT_DEAD_MESSAGE_COOLDOWN, DETECT_DEAD_MESSAGE_KEY);
            }
            else {
                instinctState.setNeedStatus(IInstinctState.NeedStatus.NONE);
                // The last prey they could see has died
                Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.finished"));
            }
            instinctState.syncNeed();
        }
        else if (hasKilled) {
            if (!covetsPrey) {
                Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.something_else", getYearnedTargetName()), 2, "wrong_prey");
            }
            covetsPrey = true;
            suddenUrgeTimer = SUDDEN_URGE_TIME;
            instinctState.syncNeed();
        }
    }
    
    private boolean isTarget(EntityLivingBase entity) {
        for (Class targetClass : targetEntityClasses) {
            if (EntityUtil.isEntityLike(entity, targetClass)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isKilled(EntityLivingBase entity) {
        return entity.isDead || entity.getHealth() <= 0;
    }
    
    // TODO: Change entity detection algorithm to allow continuing to track entities through walls below a certain distance (line of sight still required to initiate need)

    /**
     * Whether the one of the player's desired instinct prey
     * has recently been in their line of sight and nearby.
     */
    private boolean hasSeenPreyRecently(EntityPlayer player) {
        boolean seenPrey = trackedEntity != null;

        // Check for integer overflow or player reset
        if (lastPreySightTick > player.ticksExisted) {
            lastPreySightTick = player.ticksExisted;
        }
        
        // Calculate change in time since the player last saw prey
        if (seenPrey) {
            timeSinceSeenPrey = 0;
        }
        else {
            timeSinceSeenPrey += player.ticksExisted - lastPreySightTick;
            if (timeSinceSeenPrey < 0) {
                // Deal with overflow
                timeSinceSeenPrey = Integer.MAX_VALUE / 2;
            }
        }
        lastPreySightTick = player.ticksExisted;
        
        if (timeSinceSeenPrey < MAX_TICKS_OUT_OF_SIGHT) {
            return true;
        }
        return seenPrey;
    }
    
    /**
     * Updates the tracked entity.
     * If one of the player's desired instinct prey
     * is currently in their line of sight and nearby,
     * the tracked entity will be set to one of those.
     * Otherwise, the tracked entity will be null.
     */
    private void updateTrackedEntity(EntityPlayer player) {
        if (trackedEntity != null && isKilled(trackedEntity)) {
            covetsPrey = false;
            trackedEntity = null;
        }
        // Can we "see" the currently cached entity?
        if (trackedEntity != null && player.canEntityBeSeen(trackedEntity)) {
            covetsPrey = true;
        }
        else {
            trackedEntity = getNewPrey(player);
            if (trackedEntity != null) {
                covetsPrey = true;
                lastSeenPrey = trackedEntity.getClass();
            }
        }
    }
    
    /**
     * Chooses a random prey near the player
     */
    private @Nullable EntityLivingBase getNewPrey(EntityPlayer player) {
        // The player should not seek to hunt its own morph entity
        EntityLivingBase morphEntity = null;
        IMorphing morphing = Morphing.get(player);
        if (morphing != null) {
            AbstractMorph morph = morphing.getCurrentMorph();
            if (morph != null && morph instanceof EntityMorph) {
                morphEntity = ((EntityMorph)morph).getEntity(player.world);
            }
        }
        
        List<EntityLivingBase> availablePrey = new ArrayList<>();
        AxisAlignedBB aabb = new AxisAlignedBB(
                player.posX-SIGHT_RANGE, player.posY-SIGHT_RANGE, player.posZ-SIGHT_RANGE,
                player.posX+SIGHT_RANGE, player.posY+SIGHT_RANGE, player.posZ+SIGHT_RANGE
                );
        for (Class<? extends EntityLivingBase> targetEntityClass : targetEntityClasses) {
            for (EntityLivingBase possiblePrey : EntityUtil.getEntitiesAndMorphsExcluding(player, player.world, targetEntityClass, aabb)) {
                if (possiblePrey != morphEntity && !isKilled(possiblePrey) && isTarget(possiblePrey) && player.canEntityBeSeen(possiblePrey)) {
                    availablePrey.add(possiblePrey);
                }
            }
        }
        
        if (availablePrey.size() > 0) {
            return availablePrey.get(random.nextInt(availablePrey.size()));
        }
        else {
            return null;
        }
    }
}
