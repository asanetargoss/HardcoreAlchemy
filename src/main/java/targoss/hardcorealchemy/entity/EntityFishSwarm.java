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

package targoss.hardcorealchemy.entity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.morphstate.ICapabilityMorphState;
import targoss.hardcorealchemy.entity.ai.AIWanderWithTiredness;

public class EntityFishSwarm extends EntityMob {
    @CapabilityInject(ICapabilityMorphState.class)
    public static final Capability<ICapabilityMorphState> MORPH_STATE_CAPABILITY = null;
    
    // One minute to live
    public int deathTimer = 1200;

    public EntityFishSwarm(World worldIn) {
        super(worldIn);
        //Note: this entity is invisible except for the particles it produces
        this.setSize(0.8F, 0.8F);
    }
    
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(400.0D);
    }
    
    public static final String DEATH_TIMER_KEY = HardcoreAlchemy.MOD_ID + ":death_timer";
    
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        if (nbt.hasKey(DEATH_TIMER_KEY)) this.deathTimer = nbt.getInteger(DEATH_TIMER_KEY);
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger(DEATH_TIMER_KEY, this.deathTimer);
    }
    
    /*
     * This entity cannot be hurt by other living entities directly
     */
    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source.getEntity() != null && (source.getEntity() instanceof EntityLivingBase);
    }
    
    protected void initEntityAI()
    {
        this.tasks.addTask(1, new AIWanderWithTiredness(this, 1.0F, 20, 20, 60));
        this.tasks.addTask(2, new EntityAIAvoidEntity(this, EntityPlayer.class, 100.0F, 0.02F, 0.1F));
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        deathTimer--;
        
        if (this.inWater) {
            spawnBubbles();
        }
        else {
            // No point in invisible entities
            this.setHealth(0.0F);
            return;
        }
        
        // Check if the entity got caught by a player, and if so, drop fishing loot as if killed by the player
        EntityLivingBase hunter = getSuccessfulHunter();
        if (hunter != null) {
            // Instant death on contact, dropping experience and loot
            DamageSource damageSource;
            if (hunter instanceof EntityPlayer)
            {
                damageSource = DamageSource.causePlayerDamage((EntityPlayer)hunter);
                this.attackingPlayer = (EntityPlayer)hunter;
            }
            else {
                /* I generally try to make this code as generic as
                 * possible, although xp drops can only be triggered
                 * by players
                 */
                damageSource = DamageSource.causeMobDamage(hunter);
            }
            this.recentlyHit = 100;
            this.setHealth(0.0F);
            this.onDeath(damageSource);
        }
        
        if (this.deathTimer <= 0) {
            this.setDead();
        }
    }
    
    /**
     * Spawn particles every tick, increasing in frequency and size over time
     */
    public void spawnBubbles() {
        // Expand outward over the course of 10 seconds
        float bubbleSizeFraction = (float)this.ticksExisted / 200.0F;
        float bubbleCount = bubbleSizeFraction*0.5F;
        for (; bubbleCount > 1.0F; bubbleCount--) {
            this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE,
                    this.posX + (this.rand.nextFloat()-0.5F)*this.width,
                    this.posY + (this.rand.nextFloat()-0.5F)*this.height,
                    this.posZ + (this.rand.nextFloat()-0.5F)*this.width,
                    0.0D, 0.0D, 0.0D);
        }
        if (this.rand.nextFloat() < bubbleCount) {
            this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE,
                    this.posX + (this.rand.nextFloat()-0.5F)*this.width,
                    this.posY + (this.rand.nextFloat()-0.5F)*this.height,
                    this.posZ + (this.rand.nextFloat()-0.5F)*this.width,
                    0.0D, 0.0D, 0.0D);
        }
    }
    
    public boolean isHunter(EntityLivingBase entity) {
        ICapabilityMorphState morphState = entity.getCapability(MORPH_STATE_CAPABILITY, null);
        if (morphState != null && morphState.getIsFishingUnderwater()) {
            return true;
        }
        
        return false;
    }
    
    public EntityLivingBase getSuccessfulHunter() {
        List<Entity> possibleHunters = this.worldObj.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), EntitySelectors.<Entity>getTeamCollisionPredicate(this));
        
        for (Entity possibleHunter : possibleHunters) {
            if ((possibleHunter instanceof EntityLivingBase) && isHunter((EntityLivingBase)possibleHunter)) {
                return (EntityLivingBase)possibleHunter;
            }
        }
        
        return null;
    }
    
    @Override
    public void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
        Entity hunter = source.getSourceOfDamage();
        if (hunter == null) {
            return;
        }
        
        if (isHunter((EntityLivingBase)hunter)) {
            if (hunter instanceof EntityPlayer) {
                this.worldObj.spawnEntityInWorld(
                        new EntityXPOrb(this.worldObj, this.posX, this.posY, this.posZ,
                                this.getExperiencePoints((EntityPlayer)hunter)
                                )
                        );
            }
            
            LootContext.Builder lootBuilder = new LootContext.Builder((WorldServer)this.worldObj);
            if (hunter instanceof EntityPlayer) {
                    lootBuilder = lootBuilder.withLuck(
                            (float)EnchantmentHelper.getLuckOfSeaModifier((EntityPlayer)hunter) +
                            ((EntityPlayer)hunter).getLuck());
            }
            
            LootTable lootTable = this.worldObj.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING);
            for (ItemStack itemstack : lootTable.generateLootForPools(this.rand, lootBuilder.build()))
            {
                EntityItem lootItem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, itemstack);
                double dX = hunter.posX - this.posX;
                double dY = hunter.posY - this.posY;
                double dZ = hunter.posZ - this.posZ;
                double dR = (double)MathHelper.sqrt_double(dX * dX + dY * dY + dZ * dZ);
                double speedMultiplier = 0.1D;
                lootItem.motionX = dX * speedMultiplier;
                lootItem.motionY = dY * speedMultiplier;
                lootItem.motionZ = dZ * speedMultiplier;
                this.worldObj.spawnEntityInWorld(lootItem);
            }
        }
    }
    
    // Taken from EntityWaterMob
    @Override
    protected int getExperiencePoints(EntityPlayer player)
    {
        return 1 + this.worldObj.rand.nextInt(3);
    }

    // Taken from EntityWaterMob
    @Override
    public void onEntityUpdate()
    {
        int i = this.getAir();
        super.onEntityUpdate();

        if (this.isEntityAlive() && !this.isInWater())
        {
            --i;
            this.setAir(i);

            if (this.getAir() == -20)
            {
                this.setAir(0);
                this.attackEntityFrom(DamageSource.drown, 2.0F);
            }
        }
        else
        {
            this.setAir(300);
        }
    }
    
    @Override
    public void moveEntityWithHeading(float strafe, float forward) {
        super.moveEntityWithHeading(strafe, forward);
        if (!this.hasNoGravity() && !this.onGround) {
            // Undo the effects of gravity (hopefully)
            this.motionY += 0.02D;
        }
    }
    
    // Taken from EntityGuardian
    public float getBlockPathWeight(BlockPos pos)
    {
        return this.worldObj.getBlockState(pos).getMaterial() == Material.WATER ? 10.0F + this.worldObj.getLightBrightness(pos) - 0.5F : super.getBlockPathWeight(pos);
    }
    
    @Override
    public boolean isPushedByWater()
    {
        return false;
    }
    
    protected PathNavigate getNewNavigator(World worldIn)
    {
        return new PathNavigateSwimmer(this, worldIn);
    }
    
    @Override
    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.ENTITY_SQUID_AMBIENT;
    }
    
    @Override
    protected SoundEvent getHurtSound() {
        return null;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }
    
    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        return;
    }
}
