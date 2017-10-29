package targoss.hardcorealchemy.entity.ai;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.MorphingProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.player.EntityPlayer;

public class AITargetUnmorphedPlayer extends EntityAIFindEntityNearestPlayer {
    public AITargetUnmorphedPlayer(EntityAIFindEntityNearestPlayer ai) {
        super(ai.entityLiving);
        this.predicate = new Predicate<Entity>()
        {
            public boolean apply(@Nullable Entity p_apply_1_)
            {
                if (!(p_apply_1_ instanceof EntityPlayer))
                {
                    return false;
                }
                else if (((EntityPlayer)p_apply_1_).capabilities.disableDamage)
                {
                    return false;
                }
                else
                {
                    double d0 = AITargetUnmorphedPlayer.this.maxTargetRange();

                    if (p_apply_1_.isSneaking())
                    {
                        d0 *= 0.800000011920929D;
                    }

                    if (p_apply_1_.isInvisible())
                    {
                        float f = ((EntityPlayer)p_apply_1_).getArmorVisibility();

                        if (f < 0.1F)
                        {
                            f = 0.1F;
                        }

                        d0 *= (double)(0.7F * f);
                    }

                    return (double)p_apply_1_.getDistanceToEntity(AITargetUnmorphedPlayer.this.entityLiving) > d0 ? false :
                        (EntityAITarget.isSuitableTarget(AITargetUnmorphedPlayer.this.entityLiving, (EntityLivingBase)p_apply_1_, false, true)
                        && isPlayerUnmorphed((EntityPlayer)p_apply_1_));
                }
            }
        };
    }
    
    public static boolean isPlayerUnmorphed(EntityPlayer player) {
        IMorphing morphing = player.getCapability(MorphingProvider.MORPHING_CAP, null);
        if (morphing == null) {
            return true;
        }
        AbstractMorph morph = morphing.getCurrentMorph();
        return morph == null;
    }
}
