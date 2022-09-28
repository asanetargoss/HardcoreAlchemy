package targoss.hardcorealchemy.creatures.listener;

import java.util.UUID;

import com.google.common.base.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;

public class ListenerWorldHumanity extends HardcoreAlchemyListener {
    public static class NoSparkPredicate implements Predicate<EntityPlayer> {
        public static final NoSparkPredicate INSTANCE = new NoSparkPredicate();
        
        @Override
        public boolean apply(EntityPlayer player) {
            // TODO: Only return true if the player isn't affected by a spark
            return true;
        }
    }
    
    public static void onPlayerSparkCreated(EntityPlayer player) {
        // TODO: Reference in TileHeartOfForm
        // TODO: Implement
    }
    
    public static void onPlayerSparkBroken(UUID owner) {
        // TODO: Implement
    }
    
    public void onPlayerDeath() {
        // TODO: Implement (probably as event listener)
    }
}
