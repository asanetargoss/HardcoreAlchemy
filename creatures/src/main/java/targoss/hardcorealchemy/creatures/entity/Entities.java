package targoss.hardcorealchemy.creatures.entity;

import static targoss.hardcorealchemy.entity.Entities.ENTITIES;

import targoss.hardcorealchemy.render.RenderNothing;
import targoss.hardcorealchemy.util.Color;
import targoss.hardcorealchemy.util.EntityInfo;

public class Entities {
    public static final EntityInfo FISH_SWARM_ENTRY = ENTITIES.add("fish_swarm", new EntityInfo(0, EntityFishSwarm.class, new Color(0,0,0), new Color(0,0,0)));
    public static final String FISH_SWARM = FISH_SWARM_ENTRY.entityName;
    
    public static class ClientSide {
        public static final EntityInfo.ClientSide FISH_SWARM_ENTRY = targoss.hardcorealchemy.entity.Entities.ClientSide.ENTITIES.add(Entities.FISH_SWARM_ENTRY.name, new EntityInfo.ClientSide(Entities.FISH_SWARM_ENTRY, new RenderNothing.Factory()));
    }
}
