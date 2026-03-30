package targoss.hardcorealchemy.tweaks.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.heart.Heart;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts;
import targoss.hardcorealchemy.tweaks.listener.ListenerHearts;
import targoss.hardcorealchemy.util.Chat;

public class ItemHeartSacrificed extends Item {
    @CapabilityInject(ICapabilityHearts.class)
    public static final Capability<ICapabilityHearts> HEARTS_CAPABILITY = null;
    
    public final Heart heart;
    
    public ItemHeartSacrificed(Heart heart) {
        super();
        this.heart = heart;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand) {
        boolean added = false;
        ICapabilityHearts hearts = player.getCapability(HEARTS_CAPABILITY, null);
        if (hearts != null) {
            added = ListenerHearts.addHeart(HardcoreAlchemyCore.proxy.configs, player, hearts, heart, true);
            if (!added) {
                if (!player.world.isRemote) {
                    Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.heart.already_added"));
                }
            }
        }
        if (added) {
            --itemStack.stackSize;
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
        }
        else {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStack);
        }
    }
    
    protected static final String GENERIC_KEY = HardcoreAlchemyCore.MOD_ID + ".heart.sacrifice.generic";
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
    {
        String heartNamespace = heart.getRegistryName().getResourceDomain();
        String heartId = heart.getRegistryName().getResourcePath();
        {
            ITextComponent genericComponent = new TextComponentTranslation(GENERIC_KEY);
            String genericLine = genericComponent.getFormattedText();
            tooltip.add(genericLine);
        }
        {
            String perkKey = heartNamespace + ".heart." + heartId + ".sacrifice.perk";
            ITextComponent perkComponent = new TextComponentTranslation(perkKey);
            String perkLine = perkComponent.getFormattedText();
            tooltip.add(perkLine);
        }
    }
}
