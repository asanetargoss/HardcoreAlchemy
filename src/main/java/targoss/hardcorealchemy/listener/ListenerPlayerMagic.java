package targoss.hardcorealchemy.listener;

import static targoss.hardcorealchemy.HardcoreAlchemy.LOGGER;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.event.EventTakeStack;
import targoss.hardcorealchemy.network.MessageHumanity;
import targoss.hardcorealchemy.network.MessageMagic;
import targoss.hardcorealchemy.network.PacketHandler;
import targoss.hardcorealchemy.util.Chat;

public class ListenerPlayerMagic {
    /**
     *  Mods of a certain type of magic are forbidden for use by [most] permanent morphs.
     *  Generally these forms of magic are powerful, and human-like due to rigorous
     *  requirements of research and study.
     */
    public static final Set<String> HIGH_MAGIC_MODS; 
    public static final Set<String> MAGIC_ITEM_ALLOW_USE;
    public static final Set<String> MAGIC_ITEM_ALLOW_CRAFT;
    public static final Set<String> MAGIC_BLOCK_ALLOW_USE;
    
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    
    // Client-side flag. The humanity capability is not used on the client-side, so this value is authoritative.
    public static boolean canUseHighMagic = false;
    
    static {
        HIGH_MAGIC_MODS = new HashSet<String>();
        HIGH_MAGIC_MODS.add("arsmagica2");
        HIGH_MAGIC_MODS.add("ProjectE");
        HIGH_MAGIC_MODS.add("projecte");
        HIGH_MAGIC_MODS.add("astralsorcery");
        
        MAGIC_ITEM_ALLOW_USE = new HashSet<String>();
        MAGIC_ITEM_ALLOW_USE.add("projecte:item.pe_alchemical_bag");
        MAGIC_ITEM_ALLOW_USE.add("arsmagica2:workbench_upgrade");
        /* I was sorta forced to whitelist the wizard's chalk item
         * because Ars Magica forcefully sets the block state for it
         * without letting Forge events run.
         * TODO: PR Ars Magica so Forge events are called correctly
         * for that item
         */
        MAGIC_ITEM_ALLOW_USE.add("arsmagica2:chalk");
        
        MAGIC_ITEM_ALLOW_CRAFT = new HashSet<String>();
        MAGIC_ITEM_ALLOW_CRAFT.add("projecte:item.pe_covalence_dust");
        MAGIC_ITEM_ALLOW_CRAFT.add("projecte:alchemical_chest");
        MAGIC_ITEM_ALLOW_CRAFT.add("arsmagica2:magicians_workbench");
        MAGIC_ITEM_ALLOW_CRAFT.add("arsmagica2:workbench_upgrade");
        
        MAGIC_BLOCK_ALLOW_USE = new HashSet<String>();
        MAGIC_BLOCK_ALLOW_USE.add("projecte:alchemical_chest");
        MAGIC_BLOCK_ALLOW_USE.add("arsmagica2:magicians_workbench");
    }
    
    //TODO: Use Java reflection to check if these items/blocks even have a use to begin with, and only notify chat if that is the case
    
    /*TODO: Prevent using block transmutation feature of Philosopher Stone
     * Looks like the Philosopher Stone does right click logic when
     * it's supposed to only give its item action type. *shakes head*
     */
    
    @SubscribeEvent
    public void onPlayerTickMP(TickEvent.PlayerTickEvent event) {
        if (event.phase != Phase.END) {
            return;
        }
        EntityPlayer player = event.player;
        if (player.worldObj.isRemote) {
            return;
        }
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity != null) {
            capabilityHumanity.setNotifiedMagicFail(false);
        }
    }
    
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.worldObj.isRemote) {
            if (canUseHighMagic) {
                return;
            }
            if (!isAllowed(MAGIC_ITEM_ALLOW_USE, event.getItemStack())) {
                event.setCanceled(true);
            }
        }
        else {
            ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
            if (capabilityHumanity == null || capabilityHumanity.canUseHighMagic()) {
                return;
            }
            if (!isAllowed(MAGIC_ITEM_ALLOW_USE, event.getItemStack())) {
                event.setCanceled(true);
                if (!capabilityHumanity.getNotifiedMagicFail()) {
                    capabilityHumanity.setNotifiedMagicFail(true);
                    Chat.notify((EntityPlayerMP)player, "Your inhuman form prevents you from comprehending this magical item.");
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        Block block = world.getBlockState(event.getPos()).getBlock();
        EntityPlayer player = event.getEntityPlayer();
        if (world.isRemote) {
            if (canUseHighMagic) {
                return;
            }
            if (!isAllowed(MAGIC_BLOCK_ALLOW_USE, block)) {
                event.setCanceled(true);
            }
        }
        else {
            ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
            if (capabilityHumanity == null || capabilityHumanity.canUseHighMagic()) {
                return;
            }
            if (!isAllowed(MAGIC_BLOCK_ALLOW_USE, block)) {
                event.setCanceled(true);
                if (!capabilityHumanity.getNotifiedMagicFail()) {
                    capabilityHumanity.setNotifiedMagicFail(true);
                    Chat.notify((EntityPlayerMP)player, "Your inhuman form prevents you from comprehending this magical object.");
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onTakeStackPre(EventTakeStack.Pre event) {
        if (event.isCanceled()) {
            return;
        }
        LOGGER.debug("onTakeStackPre");
        ItemStack craftResult = event.slot.getStack();
        if (craftResult == null || !(event.slot instanceof SlotCrafting)) {
            return;
        }
        
        EntityPlayer player = event.player;
        if (player.worldObj.isRemote) {
            LOGGER.debug("Attempt to craft item by player: " + craftResult.getItem().getRegistryName().toString());
            if (!canUseHighMagic && !isAllowed(MAGIC_ITEM_ALLOW_CRAFT, craftResult)) {
                event.setCanceled(true);
            }
        }
        else {
            LOGGER.debug("Attempt to craft item by player: " + craftResult.getItem().getRegistryName().toString());
            ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
            if (capabilityHumanity != null &&
                    !capabilityHumanity.canUseHighMagic() &&
                    !isAllowed(MAGIC_ITEM_ALLOW_CRAFT, craftResult)) {
                event.setCanceled(true);
                if (!capabilityHumanity.getNotifiedMagicFail()) {
                    capabilityHumanity.setNotifiedMagicFail(true);
                    Chat.notify((EntityPlayerMP)player, "Your inhuman form prevents you from crafting this magical object.");
                }
            }
        }
    }
    
    public static boolean isAllowed(Set<String> whitelist, ItemStack itemStack) {
        ResourceLocation itemResource = itemStack.getItem().getRegistryName();
        return !HIGH_MAGIC_MODS.contains(itemResource.getResourceDomain()) ||
                    whitelist.contains(itemResource.toString());
    }
    
    public static boolean isAllowed(Set<String> whitelist, Block block) {
        ResourceLocation blockResource = block.getRegistryName();
        return !HIGH_MAGIC_MODS.contains(blockResource.getResourceDomain()) ||
                    whitelist.contains(blockResource.toString());
    }
}
