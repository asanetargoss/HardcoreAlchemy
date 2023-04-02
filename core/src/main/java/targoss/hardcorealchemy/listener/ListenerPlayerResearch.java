/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.listener;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.capability.research.ICapabilityResearch;
import targoss.hardcorealchemy.research.KnowledgeFact;
import targoss.hardcorealchemy.util.Chat;

public class ListenerPlayerResearch extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityResearch.class)
    public static final Capability<ICapabilityResearch> RESEARCH_CAPABILITY = null;
    
    public static void pruneResearchAfterDeath(EntityPlayer player) {
        ICapabilityResearch research = player.getCapability(RESEARCH_CAPABILITY, null);
        Set<KnowledgeFact> rememberedFacts = new HashSet<>();
        for (KnowledgeFact oldFact : research.getKnowledgeFacts()) {
            if (oldFact.getPersistsOnDeath()) {
                rememberedFacts.add(oldFact);
            }
        }
        research.setKnowledgeFacts(rememberedFacts);
    }

    /**
     * For convenience, this only sends the chat message if on the server side.
     */
    public static void acquireFactAndSendChatMessage(EntityPlayer player, KnowledgeFact fact) {
        boolean showHint;
        ICapabilityResearch research = player.getCapability(RESEARCH_CAPABILITY, null);
        if (research == null) {
            showHint = true;
        } else {
            Set<KnowledgeFact> facts = research.getKnowledgeFacts();
            showHint = facts.add(fact);
        }
        if (showHint && !player.world.isRemote) {
            Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, fact.getKnowledgeAcquireMessage());
        }
    }
}
