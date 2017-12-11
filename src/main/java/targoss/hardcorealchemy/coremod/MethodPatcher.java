package targoss.hardcorealchemy.coremod;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public abstract class MethodPatcher implements IClassTransformer {
    /**
     * @param name
     * Name of class to be transformed. Used in debug.
     * @param basicClass
     * Input bytes
     * @param flags
     * ClassWriter flag int. Generally ClassWriter.COMPUTE_MAXS, or 0 for simple patches.
     * @return
     * Output bytes
     */
    public final byte[] transformClass(String name, byte[] basicClass, int flags) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode visitor = new ClassNode();
        reader.accept(visitor, 0);
        
        for (MethodNode method : visitor.methods) {
            transformMethod(method);
        }
        
        ClassWriter writer = new ClassWriter(flags);
        visitor.accept(writer);
        byte[] newClass = writer.toByteArray();
        
        if (enableDebug()) {
            HardcoreAlchemyCoreMod.LOGGER.debug(
                    "Outputting result of patch to class '" +
                    name + "' made by '" +
                    this.getClass().getName() + "'"
                    );
            HardcoreAlchemyCoreMod.logBytesToDebug(newClass);
        }
        
        return newClass;
    }
    
    /**
     * Whether to print the transformed class bytes to the console.
     */
    public boolean enableDebug() {
        return false;
    }
    
    /**
     * Called for each method within the class
     */
    public abstract void transformMethod(MethodNode method);
}
