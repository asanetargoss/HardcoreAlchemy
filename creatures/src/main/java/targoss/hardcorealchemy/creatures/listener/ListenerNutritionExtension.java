package targoss.hardcorealchemy.creatures.listener;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.MorphDiet;
import targoss.hardcorealchemy.util.MorphDiet.Needs;
import targoss.hardcorealchemy.util.MorphExtension;
import targoss.hardcorealchemy.util.NutritionExtension;

public class ListenerNutritionExtension extends HardcoreAlchemyListener {
    public static class Wrapper extends NutritionExtension {
        @CapabilityInject(IMorphing.class)
        public static final Capability<IMorphing> MORPHING_CAPABILITY = null;
        @CapabilityInject(ICapabilityHumanity.class)
        public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
        
        public NutritionExtension delegate;

        public Wrapper(NutritionExtension delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public Needs getNeeds(EntityPlayer player) {
            if (MorphExtension.INSTANCE.isGhost(player)) {
                return MorphDiet.NO_NEEDS;
            }
            ICapabilityHumanity humanityCapability = player.getCapability(HUMANITY_CAPABILITY, null);
            if (humanityCapability == null) {
                return delegate.getNeeds(player);
            }
            if (humanityCapability.isHuman()) {
                return delegate.getNeeds(player);
            }
            IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
            AbstractMorph morph = morphing.getCurrentMorph();
            if (morph == null) {
                return delegate.getNeeds(player);
            }
            Needs needs = MorphDiet.morphDiets.get(morph.name);
            if (needs == null) {
                return delegate.getNeeds(player);
            }
            return needs;
        }
        
    }

    public void preInit(FMLPreInitializationEvent event) {
        NutritionExtension.INSTANCE = new Wrapper(NutritionExtension.INSTANCE);
    }
}
