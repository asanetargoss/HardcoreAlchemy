/*
 * Copyright 2020 asanetargoss
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

import java.util.ArrayList;
import java.util.List;

public class IDList<E> {
    protected List<E> internalList = new ArrayList<>();
    protected List<Integer> freeIDs = new ArrayList<>();

    public void setInternalList(List<E> internalList) {
        // First, remove trailing null values
        for (int i = internalList.size() - 1; i >= 0; --i) {
            if (internalList.get(i) != null) {
                break;
            }
            internalList.remove(i);
        }
        // Then find all the free IDs
        for (int i = 0; i < internalList.size(); ++i) {
            E element = internalList.get(i);
            if (element == null) {
                freeIDs.add(i);
            }
        }
        this.internalList = internalList;
    }

    public List<E> getInternalList() {
        return internalList;
    }
    
    public int add(E element) {
        if (element == null) {
            throw new NullPointerException("Null objects not supported");
        }

        if (freeIDs.isEmpty()) {
            int id = internalList.size();
            internalList.add(element);
            return id;
        } else {
            int id = freeIDs.remove(freeIDs.size() - 1);
            internalList.set(id, element);
            return id;
        }
    }

    public E get(int id) {
        if (id >= internalList.size()) {
            throw new IndexOutOfBoundsException("ID out of array bounds");
        }
        if (internalList.get(id) == null) {
            throw new IndexOutOfBoundsException("Tried to get ID of reserved null slot");
        }
        
        return internalList.get(id);
    }

    public void remove(int id) {
        if (id >= internalList.size()) {
            throw new IndexOutOfBoundsException("ID out of array bounds");
        }
        if (internalList.get(id) == null) {
            throw new IndexOutOfBoundsException("Tried to get ID of reserved null slot");
        }
        
        internalList.set(id, null);
        freeIDs.add(id);
    }
}
