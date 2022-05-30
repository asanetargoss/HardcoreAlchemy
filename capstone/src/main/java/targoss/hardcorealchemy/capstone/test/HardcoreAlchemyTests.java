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

package targoss.hardcorealchemy.capstone.test;

import java.util.ArrayList;
import java.util.List;

import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capstone.test.api.ITestSuite;
import targoss.hardcorealchemy.capstone.test.api.TestSystem;
import targoss.hardcorealchemy.capstone.test.suite.TestFoodRot;
import targoss.hardcorealchemy.capstone.test.suite.TestHumanity;
import targoss.hardcorealchemy.capstone.test.suite.TestMobLists;
import targoss.hardcorealchemy.capstone.test.suite.TestTimeFuzz;
import targoss.hardcorealchemy.capstone.test.suite.TestWorldReference;
import targoss.hardcorealchemy.config.Configs;

/**
 * Server-side tests for Hardcore Alchemy
 */
public class HardcoreAlchemyTests extends TestSystem {
    public static void runAndLogTests() {
        HardcoreAlchemyTests tests = new HardcoreAlchemyTests();
        List<String> resultsToLog = tests.testAndLogAll();
        for (String logEntry : resultsToLog) {
            HardcoreAlchemy.LOGGER.debug(logEntry);
        }
    }
    
    @Override
    public String getTestSystemName() {
        return "Hardcore Alchemy Tests";
    }
    
    public static List<Class<? extends ITestSuite>> TEST_SUITES = new ArrayList<>();
    
    static {
        TEST_SUITES.add(TestWorldReference.class);
        TEST_SUITES.add(TestFoodRot.class);
        TEST_SUITES.add(TestHumanity.class);
        TEST_SUITES.add(TestMobLists.class);
        TEST_SUITES.add(TestTimeFuzz.class);
    }
    
    @Override
    public List<Class<? extends ITestSuite>> getTestSuites() {
        return TEST_SUITES;
    }
    
    public static final Configs DEFAULT_CONFIGS = new Configs();
}
