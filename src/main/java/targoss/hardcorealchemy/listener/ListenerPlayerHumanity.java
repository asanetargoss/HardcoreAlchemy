package targoss.hardcorealchemy.listener;

import java.util.HashSet;
import java.util.Set;

import mchorse.metamorph.api.MorphAPI;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.events.MorphEvent;
import mchorse.metamorph.api.events.SpawnGhostEvent;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.capability.killcount.ProviderKillCount;
import targoss.hardcorealchemy.network.MessageHumanity;
import targoss.hardcorealchemy.network.MessageMagic;
import targoss.hardcorealchemy.network.PacketHandler;
import targoss.hardcorealchemy.util.Chat;

/*
 * TODO: Refactor forced morphing!
 */
public class ListenerPlayerHumanity {
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    public static final ResourceLocation HUMANITY_RESOURCE_LOCATION = CapabilityHumanity.RESOURCE_LOCATION;
    public static final IAttribute MAX_HUMANITY = ICapabilityHumanity.MAX_HUMANITY;
    public static final double HUMANITY_LOSS_RATE = 2.0D/24000.0D/3.0D; // Per tick
    // It takes three whole Minecraft days to lose a humanity icon and nine days to gain it back
    // So, you can only be in a morph <25% of the time if you don't want to be stuck that way
    //public static final double HUMANITY_GAIN_RATE = HUMANITY_LOSS_RATE/3.0D;
    // Kick down the humanity regen rate; the old rate seemed to not put enough pressure on the player
    public static final double HUMANITY_GAIN_RATE = HUMANITY_LOSS_RATE/12.0D;
    // Thresholds for displaying warnings when your humanity gets critically low
    public static final double HUMANITY_1MIN_LEFT = HUMANITY_LOSS_RATE*20.0D*60.0D;
    public static final double HUMANITY_2MIN_LEFT = HUMANITY_1MIN_LEFT*2.0D;
    public static final double HUMANITY_3MIN_LEFT = HUMANITY_1MIN_LEFT*3.0D;
    // Time in ticks between sending humanity value updates over the network
    public static final int HUMANITY_UPDATE_TICKS = 7;
    
    // The capability from Metamorph itself
    @CapabilityInject(IMorphing.class)
    public static final Capability<IMorphing> MORPHING_CAPABILITY = null;
    
    // Players stuck in these morphs will still be capable of all forms of magic
    public static final Set<String> HIGH_MAGIC_MORPHS = new HashSet<String>();
    
    static {
        HIGH_MAGIC_MORPHS.add("Skeleton");
        HIGH_MAGIC_MORPHS.add("PigZombie");
        HIGH_MAGIC_MORPHS.add("Blaze");
        HIGH_MAGIC_MORPHS.add("Enderman");
    }
    
    private static Item ROTTEN_FLESH;
    private static Item CHORUS_FRUIT;
    private static Item GOLDEN_APPLE;
    private static Item WITHER_APPLE;
    
    public static void postInit() {
        ROTTEN_FLESH = Item.getByNameOrId("minecraft:rotten_flesh");
        CHORUS_FRUIT = Item.getByNameOrId("minecraft:chorus_fruit");
        GOLDEN_APPLE = Item.getByNameOrId("minecraft:golden_apple");
        WITHER_APPLE = Item.getByNameOrId("adinferos:wither_apple");
        // Any more than this, and I might as well make it a hash map. >.>
    }
    
    @SubscribeEvent
    public void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (!(entity instanceof EntityPlayerMP)) {
            return;
        }
        event.addCapability(HUMANITY_RESOURCE_LOCATION, new ProviderHumanity());
        AbstractAttributeMap attributeMap = ((EntityPlayer)entity).getAttributeMap();
        if (attributeMap.getAttributeInstance(MAX_HUMANITY) == null) {
            attributeMap.registerAttribute(MAX_HUMANITY);
        }
    }
    
    @SubscribeEvent
    public void onPlayerTickMP(TickEvent.PlayerTickEvent event) {
        if (event.player.worldObj.isRemote) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP)(event.player);
        if (event.phase == Phase.START) {
            calculateHumanity(player);
        }
    }
    
    @SubscribeEvent
    public void onPlayerEat(LivingEntityUseItemEvent.Finish event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP)entity;
        ItemStack itemStack = event.getItem();
        if (itemStack == null) {
            return;
        }
        Item item = itemStack.getItem();
        // Just rotten flesh for now
        if (item != null && (item == ROTTEN_FLESH || item == CHORUS_FRUIT || item == GOLDEN_APPLE || item == WITHER_APPLE)) {
            IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
            if (morphing == null) {
                return;
            }
            ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
            if (capabilityHumanity == null || !capabilityHumanity.canMorph()) {
                return;
            }
            IAttributeInstance maxHumanity = player.getEntityAttribute(MAX_HUMANITY);
            if (maxHumanity == null) {
                return;
            }
            double newHumanity = capabilityHumanity.getHumanity();
            if (item == GOLDEN_APPLE) {
                // A testing item, but I guess it's balanced enough for regular gameplay
                newHumanity = MathHelper.clamp_double(newHumanity+HUMANITY_3MIN_LEFT, 0.0D, maxHumanity.getAttributeValue());
            }
            else {
                newHumanity = MathHelper.clamp_double(newHumanity-1.0D, 0.0D, maxHumanity.getAttributeValue());
            }
            capabilityHumanity.setHumanity(newHumanity);
            if (newHumanity == 0) {
                if (item == ROTTEN_FLESH) {
                    // If you are already in a morph, then congrats, you get to keep that morph!
                    if (morphing.getCurrentMorph() == null) {
                        // Uh oh, you're a zombie now!
                        NBTTagCompound nbt = new NBTTagCompound();
                        nbt.setString("Name", "Zombie");
                        MorphAPI.morph(player, MorphManager.INSTANCE.morphFromNBT(nbt), true);
                    }
                    if (HIGH_MAGIC_MORPHS.contains(morphing.getCurrentMorph().name)) {
                        // Allows certain morphs to still use magic due to their intelligence
                        capabilityHumanity.setHighMagicOverride(true);
                    }
                    else {
                        if (HardcoreAlchemy.isArsMagicaLoaded) {
                            ListenerPlayerMagic.eraseSpellMagic(player);
                        }
                        if (HardcoreAlchemy.isProjectELoaded) {
                            ListenerPlayerMagic.eraseEMC(player);
                        }
                    }
                }
                else if (item == CHORUS_FRUIT) {
                    // Uh oh, you're an enderman now!
                    NBTTagCompound nbt = new NBTTagCompound();
                    nbt.setString("Name", "Enderman");
                    MorphAPI.morph(player, MorphManager.INSTANCE.morphFromNBT(nbt), true);
                    capabilityHumanity.setHighMagicOverride(true);
                }
                else if (item == WITHER_APPLE) {
                    // Uh oh, you're a wither skeleton now!
                    NBTTagCompound nbt = new NBTTagCompound();
                    nbt.setString("Name", "Skeleton");
                    //TODO: actually make the player become a wither skeleton (This seems to be insufficient)
                    //      By acquiring the wither skeleton morph normally, I should be able to inspect
                    //      the NBT data and figure out what makes a wither skeleton morph work
                    nbt.setInteger("SkeletonType", 1);
                    MorphAPI.morph(player, MorphManager.INSTANCE.morphFromNBT(nbt), true);
                    capabilityHumanity.setHighMagicOverride(true);
                    //TODO: clear the withering effect
                }
                if (HardcoreAlchemy.isNutritionLoaded) {
                    ListenerPlayerDiet.updateMorphDiet(player);
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerMorph(MorphEvent.Pre event) {
        if (event.force || event.isCanceled()) {
            return;
        }
        EntityPlayer player = event.player;
        if (player.worldObj.isRemote) {
            return;
        }
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null) {
            return;
        }
        if (!capabilityHumanity.canMorph()) {
            event.setCanceled(true);
            Chat.notify((EntityPlayerMP)player, capabilityHumanity.explainWhyCantMorph());
        }
    }
    
    @SubscribeEvent
    public void onPlayerPostMorph(MorphEvent.Post event) {
        EntityPlayer player = event.player;
        if (player.worldObj.isRemote) {
            return;
        }
        // Morphing costs one hunger shank unless you're morphing back into a player
        if (!event.isDemorphing() && !event.force) {
            FoodStats foodStats = player.getFoodStats();
            foodStats.setFoodLevel(foodStats.getFoodLevel() - 2);
        }
    }
    
    @SubscribeEvent
    public void onSpawnGhost(SpawnGhostEvent.Pre event) {
        ICapabilityHumanity capabilityHumanity = event.player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null) {
            return;
        }
        if (!capabilityHumanity.canMorph()) {
            event.setCanceled(true);
        }
    }
    
    private void calculateHumanity(EntityPlayerMP player) {
        IAttributeInstance maxHumanity = player.getEntityAttribute(MAX_HUMANITY);
        if (maxHumanity == null) {
            return;
        }
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null || !capabilityHumanity.canMorph()) {
            return;
        }
        double oldHumanity = capabilityHumanity.getLastHumanity();
        double newHumanity = capabilityHumanity.getHumanity();
        // Are we in a morph? (check if the player's AbstractMorph is not null)
        IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
        if (morphing.isMorphed()) {
            // No sense in reducing humanity if the player can't morph
            if (capabilityHumanity.canMorph()) {
                // Drain humanity
                newHumanity = MathHelper.clamp_double(newHumanity-HUMANITY_LOSS_RATE, 0.0D, maxHumanity.getAttributeValue());
                capabilityHumanity.setHumanity(newHumanity);
                // If humanity reaches zero, make player stuck in a morph
                if (newHumanity <= 0) {
                    capabilityHumanity.setHasLostHumanity(true);
                    AbstractMorph morph = morphing.getCurrentMorph();
                    if (morph != null) {
                        if (HIGH_MAGIC_MORPHS.contains(morph.name)) {
                            // Allows certain morphs to still use magic due to their intelligence
                            capabilityHumanity.setHighMagicOverride(true);
                        }
                        else {
                            if (HardcoreAlchemy.isArsMagicaLoaded) {
                                ListenerPlayerMagic.eraseSpellMagic(player);
                            }
                            if (HardcoreAlchemy.isProjectELoaded) {
                                ListenerPlayerMagic.eraseEMC(player);
                            }
                        }
                    }
                    if (morph == null) {
                        // If the player isn't in a morph, give a reasonable default
                        NBTTagCompound nbt = new NBTTagCompound();
                        nbt.setString("Name", "Zombie");
                        MorphAPI.morph(player, MorphManager.INSTANCE.morphFromNBT(nbt), true);
                        if (HardcoreAlchemy.isArsMagicaLoaded) {
                            ListenerPlayerMagic.eraseSpellMagic(player);
                        }
                        if (HardcoreAlchemy.isProjectELoaded) {
                            ListenerPlayerMagic.eraseEMC(player);
                        }
                    }
                    if (HardcoreAlchemy.isNutritionLoaded) {
                        ListenerPlayerDiet.updateMorphDiet(player);
                    }
                }
            }
        }
        else {
            // Restore humanity
            newHumanity = newHumanity + HUMANITY_GAIN_RATE;
            newHumanity = MathHelper.clamp_double(newHumanity, 0.0D, maxHumanity.getAttributeValue());
            
        }
        // Notify player via chat if humanity reaches a certain threshold or is lost entirely
        sendHumanityWarnings(player, oldHumanity, newHumanity);
        capabilityHumanity.setHumanity(newHumanity);
        capabilityHumanity.setLastHumanity(newHumanity);
    }
    
    private void sendHumanityWarnings(EntityPlayerMP player, double oldHumanity, double newHumanity) {
        // We are only interested if humanity decreases
        if (newHumanity >= oldHumanity) {
            return;
        }
        //TODO: add random variety to messages
        // If humanity passes a critical threshold, display message (most urgent one first)
        if (newHumanity <= 0) {
            // Display lost humanity message
            Chat.notify((EntityPlayerMP)player, "You feel your humanity fade away.");
        }
        else if (newHumanity <= HUMANITY_3MIN_LEFT) {
            if (newHumanity <= HUMANITY_1MIN_LEFT && oldHumanity > HUMANITY_1MIN_LEFT) {
                // Display 1 minute left message
                Chat.alarm((EntityPlayerMP)player, "You panic as you realize you don't remember who you are.");
            }
            else if (newHumanity <= HUMANITY_2MIN_LEFT && oldHumanity > HUMANITY_2MIN_LEFT) {
                // Display 2 minutes left message
                Chat.notify((EntityPlayerMP)player, "You fantasize a life without thought or inhibition.");
            }
            else if (oldHumanity > HUMANITY_3MIN_LEFT) {
                // Display 3 minutes left message
                Chat.notify((EntityPlayerMP)player, "You begin to tire of remembering your human form in the back of your mind.");
            }
        }
    }
}
