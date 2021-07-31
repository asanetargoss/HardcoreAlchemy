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

package targoss.hardcorealchemy;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import targoss.hardcorealchemy.listener.ConfiguredListener;
import targoss.hardcorealchemy.listener.ListenerGuiHud;
import targoss.hardcorealchemy.listener.ListenerGuiInventory;
import targoss.hardcorealchemy.listener.ListenerRenderView;

public class ClientProxy extends CommonProxy {
    public static final ImmutableList<Class<? extends ConfiguredListener>> LISTENER_TYPES = ImmutableList.of(
            ListenerGuiHud.class,
            ListenerGuiInventory.class,
            ListenerRenderView.class
        );
    
    @Override
    public ImmutableList<Class<? extends ConfiguredListener>> getListenerTypes() {
        List<Class<? extends ConfiguredListener>> listenerTypes = new ArrayList<Class<? extends ConfiguredListener>>();
        listenerTypes.addAll(super.getListenerTypes());
        listenerTypes.addAll(LISTENER_TYPES);
        
        return ImmutableList.copyOf(listenerTypes);
    }
}
