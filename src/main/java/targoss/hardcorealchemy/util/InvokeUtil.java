/*
 * Copyright 2018 asanetargoss
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

package targoss.hardcorealchemy.util;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.annotation.Nullable;

public class InvokeUtil {
    /**
     * Gets the first occurrence of the method (either public or private;
     * doesn't matter), moving up the inheritance chain as it goes, looking
     * first in startClass and looking last in endClass.
     * 
     * includeEndClass = false will exclude endClass from search
     */
    public static Method getPrivateMethod(boolean includeEndClass, Class startClass, Class endClass, String methodName, Class... methodArgs)
            throws NoSuchMethodException, SecurityException {
        Class currentClass = startClass;
        
        for (; currentClass != endClass && currentClass != Object.class;
                currentClass = currentClass.getSuperclass()) {
            Method foundMethod = findDeclaredMethod(currentClass, methodName, methodArgs);
            if (foundMethod != null) {
                return foundMethod;
            }
        }
        
        if (includeEndClass) {
            Method foundMethod = findDeclaredMethod(currentClass, methodName, methodArgs);
            if (foundMethod != null) {
                return foundMethod;
            }
        }
        
        // Note: Not equivalent to the NoSuchMethodException thrown by Class.getMethod()
        throw new NoSuchMethodException(currentClass.getName() + "." + methodName +
                "(with parameters: " + Arrays.toString(methodArgs) + ")");
    }
    
    /**
     * Searches for the first occurrence of the method (either public or private;
     * doesn't matter), moving up the inheritance chain as it goes, looking
     * first in startClass and looking last in endClass. Returns null if unsuccessful.
     * 
     * includeEndClass = false will exclude endClass from search
     */
    public static @Nullable Method findPrivateMethod(boolean includeEndClass, Class startClass, Class endClass, String methodName, Class... methodArgs) {
        Class currentClass = startClass;
        
        for (; currentClass != endClass && currentClass != Object.class;
                currentClass = currentClass.getSuperclass()) {
            Method foundMethod = findDeclaredMethod(currentClass, methodName, methodArgs);
            if (foundMethod != null) {
                return foundMethod;
            }
        }
        
        if (includeEndClass) {
            return findDeclaredMethod(currentClass, methodName, methodArgs);
        }
        else {
            return null;
        }
    }
    
    /**
     * Similar to Class.getDeclaredMethod except doesn't throw an exception.
     * Returns null if unsuccessful.
     */
    public static @Nullable Method findDeclaredMethod(Class clazz, String methodName, Class... methodArgs) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            
            boolean argMatch = true;
            
            Class[] testArgs = method.getParameterTypes();
            if (testArgs.length != methodArgs.length) {
                continue;
            }
            for (int i = 0; i < testArgs.length; i++) {
                if (testArgs[i] != methodArgs[i]) {
                    argMatch = false;
                    break;
                }
            }
            
            if (argMatch) {
                method.setAccessible(true);
                return method;
            }
        }
        
        return null;
    }
    
    /**
     * Determines if a method exists (either public or private;
     * doesn't matter), moving up the inheritance chain as it goes, looking
     * first in startClass and looking last in endClass.
     * 
     * includeEndClass = false will exclude endClass from search
     */
    public static boolean hasPrivateMethod(boolean includeEndClass, Class startClass, Class endClass, String methodName, Class... methodArgs) {
        return null != findPrivateMethod(includeEndClass, startClass, endClass, methodName, methodArgs);
    }
}
