package targoss.hardcorealchemy.item;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import targoss.hardcorealchemy.listener.ListenerLootTester;
import targoss.hardcorealchemy.util.Chat;

public class ItemLootTester extends Item {
    public ItemLootTester() {
        super();
        setMaxDamage(16); // Vanilla code requires an item to have durability in order to be renamed
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!player.world.isRemote) {
            String loot = stack.getDisplayName();
            if (world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(loot)) == null) {
                Chat.message(Chat.Type.WARN, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.debug.loot_table.not_found", loot), 10);
                return super.onItemUse(stack, player, world, pos, hand, facing, hitX, hitY, hitZ);
            }
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityLockableLoot) {
                ((TileEntityLockableLoot)te).setLootTable(new ResourceLocation(stack.getDisplayName()), new Random().nextInt());
                Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.debug.loot_table.set", loot), 10);
                return EnumActionResult.SUCCESS;
            }
            else {
                stack.setStackDisplayName(ListenerLootTester.lastChatMessage);
                Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.debug.loot_table.item_renamed", ListenerLootTester.lastChatMessage), 10);
                return EnumActionResult.SUCCESS;
            }
        }
        return super.onItemUse(stack, player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

}
