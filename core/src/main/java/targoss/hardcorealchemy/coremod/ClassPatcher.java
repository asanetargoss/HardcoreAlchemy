/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.coremod;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;

public abstract class ClassPatcher implements IClassTransformer {
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
        if (enableDebug()) {
            HardcoreAlchemyCoreCoremod.LOGGER.debug("Attempt to patch class '" +
                    name + "' started by '" +
                    this.getClass().getName() + "'");
        }
        
        try {
            ClassReader reader = new ClassReader(basicClass);
            ClassNode visitor = new ClassNode();
            reader.accept(visitor, 0);
            
            transformClassNode(visitor);
            
            ClassWriter writer = new ClassWriter(flags);
            visitor.accept(writer);
            byte[] newClass = writer.toByteArray();
            
            if (enableDebug()) {
                HardcoreAlchemyCoreCoremod.LOGGER.debug(
                        "Outputting result of patch to class '" +
                        name + "' made by '" +
                        this.getClass().getName() + "'"
                        );
                HardcoreAlchemyCoreCoremod.logBytesToDebug(newClass);
            }

            return newClass;
        }
        catch (Exception e) {
            HardcoreAlchemyCoreCoremod.LOGGER.error(
                    "Error occurred when attempting to patch class '" +
                    name + "' using '" +
                    this.getClass().getName() + "'." +
                    "The patch has been aborted.",
                    e);
            if (enableDebug()) {
                HardcoreAlchemyCoreCoremod.LOGGER.debug(
                        "Debug is enabled. The bytecode of the unpatched " +
                        "class will follow the stack trace.");
            };
            
            if (enableDebug()) {
                HardcoreAlchemyCoreCoremod.LOGGER.debug(
                        "Outputting unpatched class '" +
                        name + "'");
                HardcoreAlchemyCoreCoremod.logBytesToDebug(basicClass);
            }
        }
        
        return basicClass;
    }
    
    /**
     * Whether to print the transformed class bytes to the console.
     */
    public boolean enableDebug() {
        return false;
    }
    
    public abstract void transformClassNode(ClassNode classNode);
}
