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

package targoss.hardcorealchemy.test.suite;

import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.test.api.ITestList;
import targoss.hardcorealchemy.test.api.ITestSuite;
import targoss.hardcorealchemy.test.api.TestList;
import targoss.hardcorealchemy.util.MiscVanilla;

public class TestFakePlayer implements ITestSuite {
    @Override
    public ITestList getTests() {
        ITestList tests = new TestList();
        
        tests.put("check server reference", this::checkServerTestReference);
        tests.put("check world available", this::checkWorldAvailable);
        
        return tests;
    }

    public boolean checkServerTestReference() {
        return HardcoreAlchemy.SERVER_REFERENCE != null && HardcoreAlchemy.SERVER_REFERENCE.get() != null;
    }

    public boolean checkWorldAvailable() {
        return MiscVanilla.getWorld() != null;
    }

}
