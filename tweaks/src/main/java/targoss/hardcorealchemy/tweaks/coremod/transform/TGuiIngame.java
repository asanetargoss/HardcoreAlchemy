package targoss.hardcorealchemy.tweaks.coremod.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;
import targoss.hardcorealchemy.tweaks.item.Items;

public class TGuiIngame extends MethodPatcher {
    protected static final String GUI_INGAME = "net.minecraft.client.gui.GuiIngame";
    protected static final ObfuscatedName RENDER_HOTBAR_ITEM = new ObfuscatedName("func_184044_a" /*renderHotbarItem*/);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(GUI_INGAME)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(RENDER_HOTBAR_ITEM.get())) {
            InsnList patch = new InsnList();
            patch.add(new VarInsnNode(Opcodes.ALOAD, 4)); // EntityPlayer
            patch.add(new VarInsnNode(Opcodes.ILOAD, 1)); // int x
            patch.add(new VarInsnNode(Opcodes.FLOAD, 3)); // float partialTicks
            patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    this.getClass().getName().replace(".", "/") + "$Hooks",
                    "shakeHotbarItems",
                    "(Lnet/minecraft/entity/player/EntityPlayer;IF)I",
                    false));
            patch.add(new VarInsnNode(Opcodes.ISTORE, 1)); // int x
            method.instructions.insert(patch);
        }
    }
    
    public static class Hooks {
        private static float lastPartialTicks = -999.0F;
        private static int slot = -1;
        private static final int SLOTS = 9;
        private static final float[] dx = new float[SLOTS];
        @CoremodHook
        public static int shakeHotbarItems(EntityPlayer player, int x, float partialTicks) {
            PotionEffect slip = player.getActivePotionEffect(Items.POTION_SLIP);
            if (slip == null) {
                return x;
            }
            if (partialTicks != lastPartialTicks) {
                lastPartialTicks = partialTicks;
                slot = -1;
            }
            ++slot;
            if (slot >= SLOTS) {
                return x;
            }
            final float rampdownTicks = 10.0F * 20.0F;
            // This is pretty cursed
            dx[slot] = dx[slot] + ((partialTicks + (slip.getAmplifier() * Math.min(rampdownTicks, slip.getDuration()) / rampdownTicks)) * (
                    ((player.getRNG().nextFloat() - 0.5F) * 1.8F) - (0.008F * dx[slot])
                ));
            return x + (int)dx[slot];
        }
    }
}
