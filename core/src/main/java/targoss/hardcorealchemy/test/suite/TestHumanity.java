/*
 * Copyright 2017-2018 asanetargoss
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

package targoss.hardcorealchemy.test.suite;

import static targoss.hardcorealchemy.test.HardcoreAlchemyTests.DEFAULT_CONFIGS;

import mchorse.metamorph.api.MorphAPI;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.test.api.ITestList;
import targoss.hardcorealchemy.test.api.ITestSuite;
import targoss.hardcorealchemy.test.api.TestList;
import targoss.hardcorealchemy.test.api.UniqueFakePlayer;
import targoss.hardcorealchemy.util.MorphState;

public class TestHumanity implements ITestSuite {

    @Override
    public ITestList getTests() {
        ITestList tests = new TestList();
        
        tests.put("morph player", this::morphPlayer);
        tests.put("player can morph at spawn", this::playerCanMorph);
        tests.put("humanity decrease", this::humanityDecrease);
        tests.put("humanity restore", this::humanityRestore);
        tests.put("humanity loss stops morphing", this::humanityLost);
        
        return tests;
    }
    
    public static final String DEFAULT_MORPH = "Chicken";
    
    public static AbstractMorph createMorph() {
        return MorphState.createMorph(DEFAULT_MORPH);
    }
    
    public boolean morphPlayer() {
        // Set morph
        FakePlayer player = UniqueFakePlayer.create();
        MorphAPI.morph(player, createMorph(), true);
        
        // Confirm morph
        IMorphing morphing = Morphing.get(player);
        return morphing.getCurrentMorph().name == DEFAULT_MORPH;
    }
    
    public boolean playerCanMorph() {
        FakePlayer player = UniqueFakePlayer.create();
        
        // Add morph to list, so player meets morphing criteria on Metamorph side
        AbstractMorph targetMorph = createMorph();
        MorphAPI.acquire(player, targetMorph);
        
        // Attempt morph
        return MorphAPI.morph(player, targetMorph, false);
    }
    
    public static void tickPlayerHumanity(EntityPlayer player) {
        ListenerPlayerHumanity listener = new ListenerPlayerHumanity();
        listener.setConfigs(DEFAULT_CONFIGS);
        listener.onPlayerTick(new PlayerTickEvent(Phase.START, player));
    }
    
    public boolean humanityDecrease() {
        // Morph player
        FakePlayer player = UniqueFakePlayer.create();
        MorphAPI.morph(player, createMorph(), true);
        
        // Compare humanity over time
        ICapabilityHumanity humanity = player.getCapability(ListenerPlayerHumanity.HUMANITY_CAPABILITY, null);
        double before = humanity.getHumanity();
        tickPlayerHumanity(player);
        double after = humanity.getHumanity();
        
        return after < before;
    }
    
    public boolean humanityRestore() {
        // Decrease humanity
        FakePlayer player = UniqueFakePlayer.create();
        ICapabilityHumanity humanity = player.getCapability(ListenerPlayerHumanity.HUMANITY_CAPABILITY, null);
        humanity.setHumanity(humanity.getHumanity()/2.0D);
        double before = humanity.getHumanity();
        tickPlayerHumanity(player);
        double after = humanity.getHumanity();
        
        return after > before;
    }
    
    public boolean humanityLost() {
        FakePlayer player = UniqueFakePlayer.create();
        // Give player morph and turn them into that morph
        AbstractMorph targetMorph = createMorph();
        MorphAPI.acquire(player, targetMorph);
        MorphAPI.morph(player, targetMorph, false);
        
        // Zero humanity
        ICapabilityHumanity humanity = player.getCapability(ListenerPlayerHumanity.HUMANITY_CAPABILITY, null);
        humanity.setHumanity(0.0D);
        
        // Tick player to allow changes to take effect
        tickPlayerHumanity(player);
        
        // The player should fail to morph
        return !MorphAPI.morph(player, null, false);
    }
}
