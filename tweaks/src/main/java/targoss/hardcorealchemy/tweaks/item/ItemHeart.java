package targoss.hardcorealchemy.tweaks.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.heart.Heart;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts;
import targoss.hardcorealchemy.tweaks.listener.ListenerHearts;
import targoss.hardcorealchemy.util.Chat;

public class ItemHeart extends Item {
    @CapabilityInject(ICapabilityHearts.class)
    public static final Capability<ICapabilityHearts> HEARTS_CAPABILITY = null;
    
    public final Heart heart;
    
    public ItemHeart(Heart heart) {
        super();
        this.heart = heart;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand) {
        boolean added = false;
        ICapabilityHearts hearts = player.getCapability(HEARTS_CAPABILITY, null);
        if (hearts != null) {
            added = ListenerHearts.addHeart(HardcoreAlchemy.proxy.configs, player, hearts, heart);
            if (!added) {
                if (!player.world.isRemote) {
                    Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.heart.already_added"));
                }
            }
        }
        if (added) {
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.75F, 1.0F);
            --itemStack.stackSize;
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
        }
        else {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStack);
        }
    }
}
