/*
 * Copyright 2017-2018 asanetargoss
 * 
 * This file is part of Hardcore Alchemy.
 * 
 * Hardcore Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.coremod;

import java.io.PrintWriter;
import java.io.StringWriter;

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
            HardcoreAlchemyCoremod.LOGGER.debug("Attempt to patch class '" +
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
                HardcoreAlchemyCoremod.LOGGER.debug(
                        "Outputting result of patch to class '" +
                        name + "' made by '" +
                        this.getClass().getName() + "'"
                        );
                HardcoreAlchemyCoremod.logBytesToDebug(newClass);
            }

            return newClass;
        }
        catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter wrapper = new PrintWriter(stringWriter);
            e.printStackTrace(wrapper);
            
            HardcoreAlchemyCoremod.LOGGER.error(
                    "Error occurred when attempting to patch class '" +
                    name + "' using '" +
                    this.getClass().getName() + "'." +
                    "The patch has been aborted.");
            if (enableDebug()) {
                HardcoreAlchemyCoremod.LOGGER.debug(
                        "Debug is enabled. The bytecode of the unpatched " +
                        "class will follow the stack trace.");
            };
            HardcoreAlchemyCoremod.LOGGER.error(stringWriter.toString());
            
            if (enableDebug()) {
                HardcoreAlchemyCoremod.LOGGER.debug(
                        "Outputting unpatched class '" +
                        name + "'");
                HardcoreAlchemyCoremod.logBytesToDebug(basicClass);
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
