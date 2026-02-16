package targoss.hardcorealchemy.tweaks.event;

import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.common.eventhandler.Event;

public class EventServerDifficulty extends Event {
    public final EnumDifficulty difficulty;
    
    public EventServerDifficulty(EnumDifficulty difficulty) {
        this.difficulty = difficulty;
    }
}
