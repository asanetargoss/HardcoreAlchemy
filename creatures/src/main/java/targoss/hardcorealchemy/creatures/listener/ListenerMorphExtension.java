package targoss.hardcorealchemy.creatures.listener;

import javax.annotation.Nonnull;

import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.MorphExtension;

public class ListenerMorphExtension extends HardcoreAlchemyListener {
    public static class Wrapper extends MorphExtension {
        @CapabilityInject(ICapabilityHumanity.class)
        public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
        
        public MorphExtension delegate;

        public Wrapper(MorphExtension delegate) {
            this.delegate = delegate;
        }
        
        public static class Dissolution {
            public static boolean isGhost(EntityLivingBase entity) {
                IIncorporealHandler incorporeal = entity.getCapability(CapabilityIncorporealHandler.CAPABILITY_INCORPOREAL, null);
                if (incorporeal != null && incorporeal.isIncorporeal()) {
                    return true;
                }
                return false;
            }
        }
        
        @Override
        public boolean canUseHighMagicWithoutBuff(EntityPlayer player) {
            if (!delegate.canUseHighMagicWithoutBuff(player)) {
                return false;
            }
            
            IMorphing morphing = Morphing.get(player);
            if (morphing == null || morphing.getCurrentMorph() == null) {
                return true;
            }
            
            ICapabilityHumanity humanity = player.getCapability(HUMANITY_CAPABILITY, null);
            if (humanity == null || humanity.isHuman()) {
                return true;
            }
            
            return false;
        }

        @Override
        public boolean isGhost(EntityLivingBase entity) {
            if (delegate.isGhost(entity)) {
                return true;
            }
            
            if (ModState.isDissolutionLoaded && Dissolution.isGhost(entity)) {
                return true;
            }
            
            return false;
        }
        
        @Override
        public boolean canMorphInto(EntityLivingBase entity) {
            String entityName = EntityList.getEntityString(entity);
            if (entityName == null) {
                return false;
            }
            if (MorphManager.isBlacklisted(entityName)) {
                return false;
            }
            return delegate.canMorphInto(entity);
        }

        @Override
        public boolean isUnmorphed(EntityPlayer player) {
            IMorphing morphing = Morphing.get(player);
            if (morphing != null && morphing.getCurrentMorph() != null) {
                return false;
            }
            return delegate.isUnmorphed(player);
        }
        
        @Override
        public boolean isMorphedAs(EntityPlayer player, Class<? extends EntityLivingBase> entityClass) {
            IMorphing morphing = Morphing.get(player);
            if (morphing == null) {
                return delegate.isMorphedAs(player, entityClass);
            }
            AbstractMorph morph = morphing.getCurrentMorph();
            if (morph == null) {
                return delegate.isMorphedAs(player, entityClass);
            }
            if (!(morph instanceof EntityMorph)) {
                return false;
            }
            EntityLivingBase morphEntity = ((EntityMorph)morph).getEntity(player.world);
            return entityClass.isInstance(morphEntity);
        }
        
        @Override
        public EntityLivingBase getEffectiveEntity(@Nonnull EntityPlayer player) {
            IMorphing morphing = Morphing.get(player);
            if (morphing == null) {
                return delegate.getEffectiveEntity(player);
            }
            AbstractMorph morph = morphing.getCurrentMorph();
            if (morph == null) {
                return delegate.getEffectiveEntity(player);
            }
            if (!(morph instanceof EntityMorph)) {
                // Incorrect, but there's no better answer
                return delegate.getEffectiveEntity(player);
            }
            return ((EntityMorph)morph).getEntity(player.world);
        }
    }

    
    public void preInit(FMLPreInitializationEvent event) {
        MorphExtension.INSTANCE = new Wrapper(MorphExtension.INSTANCE);
    }
}
