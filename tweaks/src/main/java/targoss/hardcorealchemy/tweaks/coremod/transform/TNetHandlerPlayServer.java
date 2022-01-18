package targoss.hardcorealchemy.tweaks.coremod.transform;

import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import targoss.hardcorealchemy.coremod.HardcoreAlchemyCoremod;
import targoss.hardcorealchemy.coremod.MethodPatcher;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class TNetHandlerPlayServer extends MethodPatcher {
    protected static final String NET_HANDLER_PLAY_SERVER = "net.minecraft.network.NetHandlerPlayServer";
    protected static final Pattern USE_PACKET_REGEX = Pattern.compile("Lnet/minecraft/network/play/client/CPacket(UseEntity|PlayerTryUseItem|PlayerTryUseItemOnBlock);");
    protected static final Pattern ITEM_USE_HOOK_DESC_REGEX = Pattern.compile("\\(.*\\)Lnet/minecraft/util/EnumActionResult;"); // Identifies a function that returns EnumActionResult
    protected static final ObfuscatedName PLAYER_ENTITY = new ObfuscatedName("field_147369_b" /*playerEntity*/);
    protected static final ObfuscatedName PROCESS_HELD_ITEM_CHANGE = new ObfuscatedName("func_147355_a" /*NetHandlerPlayServer::processHeldItemChange*/);
    protected static final ObfuscatedName CHECK_THREAD_AND_ENQUEUE = new ObfuscatedName("func_180031_a" /*PacketThreadUtil.checkThreadAndEnqueue*/);
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(NET_HANDLER_PLAY_SERVER)) {
            return transformClass(transformedName, basicClass, ClassWriter.COMPUTE_MAXS);
        }
        return basicClass;
    }
    
    protected @Nullable String regexGet(String s, Pattern p, Object debugObj) {
        Matcher m = p.matcher(s);
        boolean matches = m.find();
        if (enableDebug()) {
            HardcoreAlchemyCoremod.LOGGER.debug(debugObj.toString() + ": " + s + " matches " + p.toString() + "? " + matches);
        }
        if (matches) {
            String g = m.group();
            if (enableDebug()) {
                HardcoreAlchemyCoremod.LOGGER.debug("Match is " + g);
            }
            return g;
        }
        return null;
    }

    protected boolean regexMatches(String s, Pattern p, Object debugObj) {
        boolean matches = p.matcher(s).find();
        if (enableDebug()) {
            HardcoreAlchemyCoremod.LOGGER.debug(debugObj.toString() + ": " + s + " matches " + p.toString() + "? " + matches);
        }
        return matches;
    }
    
    protected void transformUseItemMethod(MethodNode method, String usePacketDesc) {
        InsnList insns = method.instructions;
        ListIterator<AbstractInsnNode> iter = insns.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode insn = iter.next();
            if (insn instanceof MethodInsnNode && regexMatches(((MethodInsnNode)insn).desc, ITEM_USE_HOOK_DESC_REGEX, insn)) {
                InsnList patch = new InsnList();
                // This code *roughly* does this:
                // EventItemUseResult.onItemUseResult(result, packetIn, this.playerEntity);
                // onItemUseResult returns void so we have to clone the result object on the stack with DUP
                patch.add(new InsnNode(Opcodes.DUP)); // EnumActionResult result
                patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // CPacket[item_use_type] packet
                patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                patch.add(new FieldInsnNode(Opcodes.GETFIELD,
                        NET_HANDLER_PLAY_SERVER.replace('.', '/'),
                        PLAYER_ENTITY.get(),
                        "Lnet/minecraft/entity/player/EntityPlayerMP;")); // EntityPlayerMP player
                patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "targoss/hardcorealchemy/tweaks/event/EventItemUseResult",
                        "onItemUseResult",
                        "(Lnet/minecraft/util/EnumActionResult;" +
                                usePacketDesc +
                                "Lnet/minecraft/entity/player/EntityPlayer;)V",
                        false)); // EventItemUseResult.onItemUseResult(result, packetIn, player)
                insns.insert(insn, patch);
            }
        }
    }
    
    protected void transformHeldItemChangeMethod(MethodNode method) {
        InsnList insns = method.instructions;
        {
            ListIterator<AbstractInsnNode> iter = insns.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode insn = iter.next();
                if (insn.getOpcode() == Opcodes.INVOKESTATIC && ((MethodInsnNode)insn).name.equals(CHECK_THREAD_AND_ENQUEUE.get())) {
                    InsnList patch = new InsnList();
                    patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // CPacketHeldItemChange
                    patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    patch.add(new FieldInsnNode(Opcodes.GETFIELD,
                            NET_HANDLER_PLAY_SERVER.replace('.', '/'),
                            PLAYER_ENTITY.get(),
                            "Lnet/minecraft/entity/player/EntityPlayerMP;")); // EntityPlayerMP player
                    patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/tweaks/event/EventHeldItemChange",
                            "onHeldItemChangePre",
                            "(Lnet/minecraft/network/play/client/CPacketHeldItemChange;" +
                                "Lnet/minecraft/entity/player/EntityPlayerMP;)" +
                                "V",
                            false));
                    
                    insns.insert(insn, patch);
                    
                    break; // Done with this patch
                }
            }
        }
        {
            ListIterator<AbstractInsnNode> iter = insns.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode insn = iter.next();
                if (insn.getOpcode() == Opcodes.RETURN) {
                    InsnList patch = new InsnList();
                    patch.add(new VarInsnNode(Opcodes.ALOAD, 1)); // CPacketHeldItemChange
                    patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    patch.add(new FieldInsnNode(Opcodes.GETFIELD,
                            NET_HANDLER_PLAY_SERVER.replace('.', '/'),
                            PLAYER_ENTITY.get(),
                            "Lnet/minecraft/entity/player/EntityPlayerMP;")); // EntityPlayerMP player
                    patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "targoss/hardcorealchemy/tweaks/event/EventHeldItemChange",
                            "onHeldItemChangePost",
                            "(Lnet/minecraft/network/play/client/CPacketHeldItemChange;" +
                                    "Lnet/minecraft/entity/player/EntityPlayerMP;)" +
                                    "V",
                            false));
                    
                    insns.insertBefore(insn, patch);
                    
                    break; // Done with this patch
                }
            }
        }
    }

    @Override
    public void transformMethod(MethodNode method) {
        // Look for methods in the vanilla client packet handling code that involve the player using items
        String usePacketDesc = regexGet(method.desc, USE_PACKET_REGEX, method);
        if (usePacketDesc != null) {
            transformUseItemMethod(method, usePacketDesc);
        }
        
        if (method.name.equals(PROCESS_HELD_ITEM_CHANGE.get())) {
            transformHeldItemChangeMethod(method);
        }
    }

}
