package targoss.hardcorealchemy.tweaks.listener;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.VirtualCapabilityManager;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.heart.Heart;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.tweaks.HardcoreAlchemyTweaks;
import targoss.hardcorealchemy.tweaks.capability.hearts.CapabilityHearts;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts;
import targoss.hardcorealchemy.tweaks.capability.hearts.ProviderHearts;
import targoss.hardcorealchemy.tweaks.capability.hearts.StorageHearts;
import targoss.hardcorealchemy.tweaks.network.MessageHearts;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.util.MorphExtension;

public class ListenerHearts extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityHearts.class)
    public static final Capability<ICapabilityHearts> HEARTS_CAPABILITY = null;
    protected Random random = new Random();
    public static final int HEART_REMOVE_CHANCE = 5;
    
    /**
     * Updates all health modifiers associated with the hearts mechanic.
     * 
     * - Disables all modifiers if the hearts mechanic is disabled
     * - Sets a negative base health modifier based on difficulty level
     * - Sets positive health modifiers for each applied heart
     * - Cleans up modifiers for missing heart IDs
     */
    public static void updateHeartModifiers(Configs configs, EntityPlayer player, @Nullable ICapabilityHearts hearts) {
        IAttributeInstance maxHealth = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH);
        {
            AttributeModifier baseModifier = Heart.getBaseModifier(configs, player.world);
            maxHealth.removeModifier(baseModifier.getID());
            if (configs.base.enableHearts) {
                maxHealth.applyModifier(baseModifier);
            }
        }
        if (hearts != null) {
            for (Heart heart : hearts.get()) {
                AttributeModifier modifier = heart.getModifier();
                maxHealth.removeModifier(modifier.getID());
                if (configs.base.enableHearts) {
                    maxHealth.applyModifier(modifier);
                }
            }
            for (ResourceLocation removedHeart : hearts.getRemoved()) {
                UUID modifierID = Heart.getModifierIDForRegistryName(removedHeart);
                maxHealth.removeModifier(modifierID);
            }
            hearts.getRemoved().clear();
            
            if (!player.world.isRemote) {
                HardcoreAlchemyTweaks.proxy.messenger.sendTo(new MessageHearts(hearts), (EntityPlayerMP)player);
            }
        }
    }
    
    public static boolean addHeart(Configs configs, EntityPlayer player, ICapabilityHearts hearts, Heart heart) {
        boolean added = hearts.get().add(heart);
        if (added) {
            updateHeartModifiers(configs, player, hearts);
            float healthAfterBonus = (float)(player.getHealth() + heart.getModifier().getAmount());
            player.setHealth(healthAfterBonus);
        }
        return added;
    }
    
    public static boolean removeHeart(Configs configs, EntityPlayer player, ICapabilityHearts hearts, Heart heart) {
        boolean removed = hearts.get().remove(heart);
        if (removed) {
            updateHeartModifiers(configs, player, hearts);
        }
        return removed;
    }
    
    @Override
    public void registerCapabilities(CapabilityManager manager, VirtualCapabilityManager virtualManager) {
        manager.register(ICapabilityHearts.class, new StorageHearts(), CapabilityHearts.class);
    }

    @SubscribeEvent
    public void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(CapabilityHearts.RESOURCE_LOCATION, new ProviderHearts());
        }
    }
    
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer oldPlayer = event.getOriginal();
        EntityPlayer newPlayer = event.getEntityPlayer();
        @Nullable ICapabilityHearts hearts = newPlayer.getCapability(HEARTS_CAPABILITY, null);
        if (hearts != null) {
            CapUtil.copyOldToNew(HEARTS_CAPABILITY, oldPlayer, newPlayer);
            if (event.isWasDeath()) {
                // Chance to remove heart
                if (random.nextInt(HEART_REMOVE_CHANCE) == 0) {
                    int heartIndexToRemove = random.nextInt(hearts.get().size());
                    System.out.println("heart count: " + hearts.get().size());
                    System.out.println("to remove: " + heartIndexToRemove);
                    int i = 0;
                    Heart heartToRemove = null;
                    for (Heart heart : hearts.get()) {
                        if (i == heartIndexToRemove) {
                            heartToRemove = heart;
                            break;
                        }
                        ++i;
                    }
                    if (heartToRemove != null) {
                        removeHeart(coreConfigs, newPlayer, hearts, heartToRemove);
                    }
                }
                // Clear list of acquired shards for this life
                hearts.getAcquiredShards().clear();
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) {
            return;
        }
        
        syncFullPlayerCapabilities((EntityPlayerMP)(event.player));
    }
    
    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer)) {
            return;
        }
        if (((EntityPlayer)event.getEntity()).world.isRemote) {
            return;
        }
        
        syncFullPlayerCapabilities((EntityPlayerMP)(event.getEntity()));
    }
    
    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerChangedDimensionEvent event) {
        // Fix health modifier desync when switching dimensions
        // I'm not sure why Minecraft insists on clearing modifiers on change dimension,
        // but no use arguing with it.
        @Nullable ICapabilityHearts hearts = event.player.getCapability(HEARTS_CAPABILITY, null);
        updateHeartModifiers(coreConfigs, event.player, hearts);
    }
    
    public void syncFullPlayerCapabilities(EntityPlayerMP player) {
        @Nullable ICapabilityHearts hearts = player.getCapability(HEARTS_CAPABILITY, null);
        updateHeartModifiers(coreConfigs, player, hearts);
    }
    
    public static class ClientSide extends HardcoreAlchemyListener {
        protected int left_height = GuiIngameForge.left_height;
        protected Random rand = new Random();
        
        protected long lastSystemTime;
        protected long healthUpdateCounter;
        protected int playerHealth;
        protected int lastPlayerHealth;
        
        protected long heartsUpdateCounter;
        protected int playerMaxHealth;
        protected int lastPlayerMaxHealth;
        
        @SubscribeEvent(priority=EventPriority.LOWEST)
        public void onRenderHeartsPre(RenderGameOverlayEvent.Pre event) {
            if (event.getType() != ElementType.HEALTH) {
                return;
            }
            left_height = GuiIngameForge.left_height;
        }

        /* Render heart overlay differently for vanilla vs Mantle
         * Currently this only works for up to 10 hearts, which is fine for now
         */
        @SubscribeEvent(priority=EventPriority.HIGHEST)
        public void onRenderHeartsPost(RenderGameOverlayEvent.Post event) {
            EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
            if (player.isCreative()) {
                return;
            }
            if (MorphExtension.INSTANCE.isGhost(player)) {
                return;
            }
            if (player.isPotionActive(MobEffects.POISON)) {
                // Heart variants are not implemented
                return;
            }
            if (player.isPotionActive(MobEffects.WITHER)) {
                // Heart variants are not implemented
                return;
            }
            
            ICapabilityHearts hearts = player.getCapability(HEARTS_CAPABILITY, null);
            if (hearts == null) {
                return;
            }
            int lastHeart = Math.min(9, hearts.get().size() - 1);
            if (lastHeart < 0) {
                return;
            }
            int health = MathHelper.ceil(player.getHealth());
            int maxHealth = MathHelper.ceil(player.getMaxHealth());
            int firstHeart = (health <= 20 || !ModState.isMantleLoaded) ? 0 : (((health + 1) / 2) % 10);
            firstHeart = Math.min(firstHeart, lastHeart);
            if (firstHeart > lastHeart) {
                return;
            }
            ArrayList<Heart> heartArray = new ArrayList<>();
            heartArray.addAll(hearts.get());

            Minecraft mc = Minecraft.getMinecraft();
            GuiIngame gui = mc.ingameGUI;
            int updateCounter = gui.getUpdateCounter();

            boolean highlight = healthUpdateCounter > (long)updateCounter && (healthUpdateCounter - (long)updateCounter) / 3L %2L == 1L;
            boolean heartsHighlight = !highlight &&
                                heartsUpdateCounter > (long)updateCounter && (heartsUpdateCounter - (long)updateCounter) / 3L %2L == 1L;
            if (health < this.playerHealth && player.hurtResistantTime > 0)
            {
                this.lastSystemTime = Minecraft.getSystemTime();
                this.healthUpdateCounter = (long)(updateCounter + 20);
            }
            else if (health > this.playerHealth && player.hurtResistantTime > 0)
            {
                this.lastSystemTime = Minecraft.getSystemTime();
                this.healthUpdateCounter = (long)(updateCounter + 10);
            }
            if (maxHealth > this.playerMaxHealth) {
                this.heartsUpdateCounter = (long)(updateCounter + 10);
            }
            this.playerMaxHealth = maxHealth;
            if (Minecraft.getSystemTime() - this.lastSystemTime > 1000L)
            {
                this.playerHealth = health;
                this.lastPlayerHealth = health;
                this.lastSystemTime = Minecraft.getSystemTime();
            }
            this.playerHealth = health;
            int healthLast = this.lastPlayerHealth;
            
            ScaledResolution resolution = event.getResolution();
            int width = resolution.getScaledWidth();
            int height = resolution.getScaledHeight();
            int left = width / 2 - 91;
            int top = height - left_height;
            float absorb = MathHelper.ceil(player.getAbsorptionAmount());
            int healthRows = MathHelper.ceil((maxHealth + absorb) / 2.0F / 10.0F);
            int rowHeight = Math.max(10 - (healthRows - 2), 3);
            int regen = -1;
            if (player.isPotionActive(MobEffects.REGENERATION))
            {
                regen = updateCounter % 25;
            }

            GlStateManager.enableBlend();
            
            ResourceLocation vanillaTileset = Gui.ICONS;
            mc.getTextureManager().bindTexture(vanillaTileset);
            ResourceLocation currentTileset = vanillaTileset;

            // Set the seed
            rand.setSeed(updateCounter * 312871);
            // Draw the heart outlines for gaining a heart upgrade, using the vanilla highlight texture
            if (heartsHighlight) {
                final int TOP =  9 * (mc.world.getWorldInfo().isHardcoreModeEnabled() ? 5 : 0);
                for (int i = MathHelper.ceil((maxHealth + absorb) / 2.0F) - 1; i >= 0; --i)
                {
                    int row = MathHelper.ceil((float)(i + 1) / 10.0F) - 1;
                    int x = left + i % 10 * 8;
                    int y = top - row * rowHeight;
    
                    if (health <= 4) {
                        y += rand.nextInt(2);
                    }
                    if (i == regen) {
                        y -= 2;
                    }
    
                    gui.drawTexturedModalRect(x, y, 25, TOP, 9, 9);
                }
            }

            // Reset the seed
            rand.setSeed(updateCounter * 312871);
            // Overlay special heart textures on top of the vanilla heart textures
            for (int i = MathHelper.ceil((maxHealth + absorb) / 2.0F) - 1; i >= 0; --i)
            {
                int row = MathHelper.ceil((float)(i + 1) / 10.0F) - 1;
                int x = left + i % 10 * 8;
                int y = top - row * rowHeight;

                if (health <= 4) {
                    y += rand.nextInt(2);
                }
                // RNG has been set. Decide if there is a special heart that needs to be rendered here.
                if (i > lastHeart || i < firstHeart) {
                    continue;
                }
                Heart heart = heartArray.get(i);
                if (heart.tileset != currentTileset) {
                    currentTileset = heart.tileset;
                    mc.getTextureManager().bindTexture(heart.tileset);
                }
                if (i == regen) {
                    y -= 2;
                }

                if (highlight || heartsHighlight)
                {
                    if (i * 2 + 1 < healthLast) {
                        gui.drawTexturedModalRect(x, y, heart.highlightTileU, heart.highlightTileV, 9, 9);
                    }
                    else if (i * 2 + 1 == healthLast) {
                        gui.drawTexturedModalRect(x, y, heart.highlightTileU, heart.highlightTileV, 9, 9);
                    }
                }
                else {
                    if (i * 2 + 1 < health) {
                        gui.drawTexturedModalRect(x, y, heart.tileU, heart.tileV, 9, 9);
                    }
                    else if (i * 2 + 1 == health) {
                        gui.drawTexturedModalRect(x, y, heart.tileU, heart.tileV, 5, 9);
                    }
                }
            }
            
            if (currentTileset != vanillaTileset) {
                mc.getTextureManager().bindTexture(vanillaTileset);
            }

            // HACK: Don't disable blend if Tough As Nails is altering the GUI
            if (!ModState.isTanLoaded) {
                GlStateManager.disableBlend();
            }
        }
    }
}
