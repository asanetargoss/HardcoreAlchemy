package targoss.hardcorealchemy.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;

public class AISpiderTargetMobOrMorph<T extends EntityLivingBase> extends AIAttackTargetMobOrMorph<T> {

    public AISpiderTargetMobOrMorph(EntitySpider.AISpiderTarget<T> AIIgnoringMorph) {
        super(AIIgnoringMorph);
    }
    
    public boolean shouldExecute()
    {
        float f = this.taskOwner.getBrightness(1.0F);
        return f >= 0.5F ? false : super.shouldExecute();
    }
}
