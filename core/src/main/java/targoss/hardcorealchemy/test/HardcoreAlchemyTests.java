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

package targoss.hardcorealchemy.test;

import java.util.ArrayList;
import java.util.List;

import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.test.api.ITestSuite;
import targoss.hardcorealchemy.test.api.TestSystem;
import targoss.hardcorealchemy.test.suite.TestHumanity;
import targoss.hardcorealchemy.test.suite.TestMobLists;
import targoss.hardcorealchemy.test.suite.TestTimeFuzz;
import targoss.hardcorealchemy.test.suite.TestWorldReference;

/**
 * Server-side tests for Hardcore Alchemy
 */
public class HardcoreAlchemyTests extends TestSystem {
    public void runAndLogTests() {
        List<String> resultsToLog = testAndLogAll();
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
