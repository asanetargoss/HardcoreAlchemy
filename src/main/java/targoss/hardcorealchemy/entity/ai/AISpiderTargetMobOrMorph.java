package targoss.hardcorealchemy.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;

public class AISpiderTargetMobOrMorph<T extends EntityLivingBase> extends AIAttackTargetMobOrMorph<T> {

    public AISpiderTargetMobOrMorph(EntityAINearestAttackableTarget<T> AIIgnoringMorph) {
        super(AIIgnoringMorph);
    }
    
    public boolean shouldExecute()
    {
        float f = this.taskOwner.getBrightness(1.0F);
        return f >= 0.5F ? false : super.shouldExecute();
    }
}
