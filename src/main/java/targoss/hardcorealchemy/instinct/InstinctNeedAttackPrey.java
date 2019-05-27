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

import static targoss.hardcorealchemy.util.Serialization.NBT_STRING_ID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
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
    
    /** Indicates that the player needs to kill something */
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
    protected static class EntityTargetInfo<T extends EntityLivingBase> {
        public final Class<T> entityClass;
        public final Predicate<? super T> filter;
        protected final int hash;
        
        public EntityTargetInfo(Class<T> targetClass, @Nullable Predicate<? super T> targetSelector) {
            this.entityClass = targetClass;
            this.filter = targetSelector;
            int hash = this.entityClass.hashCode();
            if (this.filter != null) {
                hash = hash * 31 + this.filter.hashCode();
            }
            this.hash = hash;
        }
        
        public boolean isValidTarget(EntityLivingBase entity) {
            EntityLivingBase effectiveEntity = EntityUtil.getEffectiveEntity(entity);
            if (!entityClass.isAssignableFrom(effectiveEntity.getClass())) {
                return false;
            }
            if (filter != null && !filter.apply((T)effectiveEntity)) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                return true;
            }
            
            if (obj == null || !(obj instanceof EntityTargetInfo)) {
                return false;
            }
            
            EntityTargetInfo other = (EntityTargetInfo)obj;
            
            return (entityClass == other.entityClass &&
                    filter == other.filter);
        }
        
    }
    // This will contain information from just the AI at first, but more entries will be added as entities are identified as prey
    private Set<EntityTargetInfo<? extends EntityLivingBase>> entityTargetTypes = new HashSet<>();
    private static final int MAX_SIMPLE_TARGET_TYPES_SERIALIZED = 10;
    
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
        if (entityTargetTypes.size() == 0) {
            return null;
        }
        
        int indexToSelect = random.nextInt(entityTargetTypes.size());
        int i = 0;
        
        String chosenEntityName = null;
        Class<? extends EntityLivingBase> chosenEntityClass = null;
        for (EntityTargetInfo targetInfo : entityTargetTypes) {
            if (i++ == indexToSelect) {
                chosenEntityClass = targetInfo.entityClass;
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
        entityTargetTypes.clear();
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
                entityTargetTypes.add(new EntityTargetInfo(targetClass, ((EntityAINearestAttackableTarget)task).targetEntitySelector));
                
                if (targetClass == EntityPlayer.class) {
                    /* Also add non-player humans from MobLists.getHumans()
                     * This allows more options for things to kill
                     */
                    for (Class<? extends EntityLivingBase> humanClass : getHumanClasses()) {
                        entityTargetTypes.add(new EntityTargetInfo(humanClass, null));
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
    private static final String NBT_KNOWN_TARGETS = "knownTargets";
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setBoolean(NBT_COVETS_PREY, covetsPrey);
        nbt.setInteger(NBT_TIME_SINCE_SEEN_PREY, timeSinceSeenPrey);
        if (lastSeenPrey != null) {
            String entityString = EntityList.getEntityStringFromClass(lastSeenPrey);
            if (entityString != null) {
                nbt.setString(NBT_LAST_SEEN_PREY, entityString);
            }
        }
        nbt.setBoolean(NBT_HAS_KILLED, hasKilled);
        nbt.setInteger(NBT_SUDDEN_URGE_TIMER, suddenUrgeTimer);
        
        NBTTagList knownTargetsList = new NBTTagList();
        // Avoid caching too many things
        int targetCacheAllowance = MAX_SIMPLE_TARGET_TYPES_SERIALIZED;
        for (EntityTargetInfo targetInfo : entityTargetTypes) {
            if (targetInfo.filter != null) {
                // Can't serialize these
                continue;
            }
            String entityString = EntityList.getEntityStringFromClass(targetInfo.entityClass);
            if (entityString != null && !entityString.isEmpty()) {
                knownTargetsList.appendTag(new NBTTagString(entityString));
                if (--targetCacheAllowance <= 0) {
                    break;
                }
            }
        }
        nbt.setTag(NBT_KNOWN_TARGETS, knownTargetsList);
        
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        covetsPrey = nbt.getBoolean(NBT_COVETS_PREY);
        timeSinceSeenPrey = nbt.getInteger(NBT_TIME_SINCE_SEEN_PREY);
        if (nbt.hasKey(NBT_LAST_SEEN_PREY)) {
            String entityString = nbt.getString(NBT_LAST_SEEN_PREY);
            // If you don't check the string is valid, vanilla could return a pig instead as the entity ID, which we don't want
            if (EntityList.isStringValidEntityName(entityString)) {
                Class<? extends Entity> possibleLastSeenPrey = EntityList.getClassFromID(EntityList.getIDFromString(entityString));
                if (EntityLivingBase.class.isAssignableFrom(possibleLastSeenPrey)) {
                    lastSeenPrey = (Class<? extends EntityLivingBase>)possibleLastSeenPrey;
                }
            }
        }
        hasKilled = nbt.getBoolean(NBT_HAS_KILLED);
        suddenUrgeTimer = nbt.getInteger(NBT_SUDDEN_URGE_TIMER);
        {
            NBTTagList knownTargetsList = nbt.getTagList(NBT_KNOWN_TARGETS, NBT_STRING_ID);
            int n = Math.min(knownTargetsList.tagCount(), MAX_SIMPLE_TARGET_TYPES_SERIALIZED);
            for (int i = 0; i < n; i++) {
                String knownTargetString = knownTargetsList.getStringTagAt(i);
                if (knownTargetString == "") {
                    continue;
                }
                Class<? extends Entity> entityClass = EntityList.NAME_TO_CLASS.get(knownTargetString);
                if (entityClass == null || !EntityLivingBase.class.isAssignableFrom(entityClass)) {
                    continue;
                }
                entityTargetTypes.add(new EntityTargetInfo((Class<? extends EntityLivingBase>)entityClass, null));
            }
        }
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
            else if (!covetsPrey && covetedPrey && lastTrackedEntity != null) {
                if (!player.world.isRemote) {
                    instinctState.setNeedStatus(InstinctState.NeedStatus.NONE);
                    Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.instinct.attack_prey.gone"));
                    instinctState.syncNeed();
                }
            }
        }
        
        sawPrey = hasSeenPrey;
    }
    
    @Override
    public void afterKill(IInstinctState instinctState, EntityLivingBase entity) {
        EntityPlayer player = instinctState.getPlayer();
        if (isTarget(entity) || (trackedEntity != null && entity == trackedEntity)) {
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
        // The AI predicates discard entities that are dead, but we may need to know if this entity WAS a valid target at some point.
        boolean wasDead = entity.isDead;
        float health = entity.getHealth();
        entity.isDead = false;
        entity.setHealth(Float.MIN_VALUE);
        boolean validTarget = false;
        for (EntityTargetInfo targetInfo : entityTargetTypes) {
            if (targetInfo.isValidTarget(entity)) {
                validTarget = true;
                break;
            }
        }
        entity.isDead = wasDead;
        entity.setHealth(health);
        
        return validTarget;
    }
    
    private boolean isKilled(EntityLivingBase entity) {
        return entity.getHealth() <= 0;
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
        if (trackedEntity == null || isKilled(trackedEntity)) {
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
        for (EntityTargetInfo targetInfo : entityTargetTypes) {
            for (EntityLivingBase possiblePrey : (List<EntityLivingBase>)EntityUtil.getEntitiesAndMorphsExcluding(player, player.world, targetInfo.entityClass, aabb, targetInfo.filter)) {
                if (possiblePrey != morphEntity && !isKilled(possiblePrey) && player.canEntityBeSeen(possiblePrey)) {
                    availablePrey.add(possiblePrey);
                }
            }
        }
        
        if (availablePrey.size() > 0) {
            EntityLivingBase chosenPrey = availablePrey.get(random.nextInt(availablePrey.size()));
            // Also add this to the list of valid targets, if it is not present (reduces issues with unreliable targeting lambdas)
            // Because it's a set, no need to check for duplicates
            // TODO: AAAAA! It's still complaining about wanting to kill squid instead, when killing squid!
            entityTargetTypes.add(new EntityTargetInfo(chosenPrey.getClass(), null));
            return chosenPrey;
        }
        else {
            return null;
        }
    }
}
