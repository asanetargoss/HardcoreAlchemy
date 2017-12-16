package targoss.hardcorealchemy.test.suite;

import java.util.ArrayList;
import java.util.List;

import mchorse.metamorph.api.MorphAPI;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.capability.humanity.ForcedMorph;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.LostMorphReason;
import targoss.hardcorealchemy.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.test.HardcoreAlchemyTests;
import targoss.hardcorealchemy.test.api.UniqueFakePlayer;
import targoss.hardcorealchemy.test.api.ITestList;
import targoss.hardcorealchemy.test.api.ITestSuite;
import targoss.hardcorealchemy.test.api.TestList;

public class TestHumanity implements ITestSuite {

    @Override
    public ITestList getTests() {
        ITestList tests = new TestList();
        
        tests.put("morph player", this::morphPlayer);
        tests.put("player can morph at spawn", this::playerCanMorph);
        tests.put("humanity decrease", this::humanityDecrease);
        tests.put("humanity restore", this::humanityRestore);
        tests.put("humanity loss stops morphing", this::humanityLost);
        tests.put("becoming mage stops morphing", this::mageCantMorph);
        
        return tests;
    }
    
    public static final String DEFAULT_MORPH = "Chicken";
    
    public static AbstractMorph createMorph() {
        return ForcedMorph.createMorph(DEFAULT_MORPH);
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
        (new ListenerPlayerHumanity()).onPlayerTickMP(new PlayerTickEvent(Phase.START, player));
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
    
    public boolean mageCantMorph() {
        FakePlayer player = UniqueFakePlayer.create();
        // Give player a potential morph
        AbstractMorph targetMorph = createMorph();
        MorphAPI.acquire(player, targetMorph);
        
        // Make player become a mage
        ForcedMorph.forceForm(player, LostMorphReason.MAGE, (AbstractMorph)null);
        
        // Attempt morph
        return !MorphAPI.morph(player, targetMorph, false);
    }
}
