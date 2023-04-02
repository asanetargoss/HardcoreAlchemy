/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Capstone.
 *
 * Hardcore Alchemy Capstone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Hardcore Alchemy Capstone is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Hardcore Alchemy Capstone.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capstone.test.api;

import java.util.ArrayList;

public class TestList extends ArrayList<ITestList.TestEntry> implements ITestList {
    private static final long serialVersionUID = -1075123672610104867L;

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
