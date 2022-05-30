/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.capstone.test.suite;

import net.minecraft.entity.monster.EntityZombie;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.capstone.test.api.ITestList;
import targoss.hardcorealchemy.capstone.test.api.ITestSuite;
import targoss.hardcorealchemy.capstone.test.api.TestList;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.MiscVanilla;

public class TestWorldReference implements ITestSuite {
    @Override
    public ITestList getTests() {
        ITestList tests = new TestList();
        
        tests.put("check server reference", this::checkServerTestReference);
        tests.put("check world available", this::checkWorldAvailable);
        tests.put("check worldless entity creation", this::checkEntityCreation);
        
        return tests;
    }

    public boolean checkServerTestReference() {
        return HardcoreAlchemyCore.SERVER_REFERENCE != null && HardcoreAlchemyCore.SERVER_REFERENCE.get() != null;
    }

    public boolean checkWorldAvailable() {
        return MiscVanilla.getWorld() != null;
    }
    
    public boolean checkEntityCreation() {
        return EntityUtil.createEntity(EntityZombie.class) != null;
    }
}
