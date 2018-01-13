/**
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

package targoss.hardcorealchemy.test.api;

import java.util.ArrayList;
import java.util.List;

import targoss.hardcorealchemy.test.api.ITestList.TestEntry;

public class TestList extends ArrayList<ITestList.TestEntry> implements ITestList {
    @Override
    public void put(String name, Test test) {
        this.add(new TestEntry(name, test));
    }
    
    @Override
    public void putIf(String name, Test test, boolean shouldAdd) {
        if (shouldAdd) {
            this.add(new TestEntry(name, test));
        }
    }
}
