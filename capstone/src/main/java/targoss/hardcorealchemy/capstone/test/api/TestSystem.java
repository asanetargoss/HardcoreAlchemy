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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * System to run tests inside of a runtime environment.
 * 
 * General use involves implementing getTestSuites()
 * and getTestSystemName(), then calling testAndLogAll() with the implementor.
 */
public abstract class TestSystem {
    
    public abstract List<Class<? extends ITestSuite>> getTestSuites();
    
    public abstract String getTestSystemName();
    
    /*TODO: Allow suites to have custom names so they can be
     * anonymous classes (recommend also not using maps or sets,
     * and passing an instance rather than a class so that parameters
     * can be passed)
     */
    /*TODO: Consider a log setting to display a list of every
     * test that was run
     */
    /*TODO: Consider more flexible test execution (choice of whether
     * tests should be run when other tests succeed, and allow multiple
     * test checks within the same method to reduce repeated work)
     */
    
    /**
     * Runs all tests suites provided by getTestSuites().
     * Instantiates each ITestSuite with the default constructor.
     * Runs the methods provided by ITestSuite.getTests() in the provided
     * order until a failed test (boolean false) is reached.
     * Logs informational text to the output list.
     */
    public List<String> testAndLogAll() {
        List<String> logResults = new ArrayList<>();
        
        String suiteName = getTestSystemName();
        if (suiteName != null) {
            logResults.add("Running all test suites for '" + suiteName + "'...");
        }
        else {
            logResults.add("Running all test suites for unnamed...");
        }
        Map<Class<? extends ITestSuite>, TestStatus> testResults = runAllTestSuites();
        for (Map.Entry<Class<? extends ITestSuite>, TestStatus> testResult : testResults.entrySet()) {
            Class<? extends ITestSuite> suite = testResult.getKey();
            TestStatus testStatus = testResult.getValue();
            String failedTestInfo;
            if (testStatus.failedTestName == null) {
                failedTestInfo = "Success!";
            }
            else {
                failedTestInfo = "Failed test: '" + testStatus.failedTestName + "'";
            }
            logResults.add(failedTestInfo +
                    " (" + testStatus.successfulTests + " of " + testStatus.totalTests + " passed)" +
                    " (" + suite.getTypeName() + ")");
        }
        
        return logResults;
    }
    
    /**
     * Runs all tests suites provided by getTestSuites().
     * Instantiates each ITestSuite with the default constructor.
     * Runs the methods provided by ITestSuite.getTests() in the provided
     * order until a failed test (boolean false) is reached.
     */
    public Map<Class<? extends ITestSuite>, TestStatus> runAllTestSuites() {
        List<Class<? extends ITestSuite>> testSuites = getTestSuites();
        Map<Class<? extends ITestSuite>, TestStatus> testSuccesses = new HashMap<>();
        for (Class<? extends ITestSuite> testSuite : testSuites) {
            testSuccesses.put(testSuite, runTestSuite(testSuite));
        }
        return testSuccesses;
    }
    
    /**
     * Instantiates ITestSuite of given class with the default constructor.
     * Runs the methods provided by ITestSuite.getTests() in the provided
     * order until a failed test (boolean false) is reached.
     */
    public static TestStatus runTestSuite(Class<? extends ITestSuite> testSuite) {
        TestStatus status = new TestStatus();
        
        ITestSuite instance = null;
        try {
            instance = testSuite.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        
        if (instance != null) {
            ITestList suiteTests = instance.getTests();
            status.totalTests = suiteTests.size();
            
            for (ITestList.TestEntry test : suiteTests) {
                boolean result = test.evaluate();
                if (result) {
                    status.successfulTests++;
                }
                else {
                    status.failedTestName = test.getName();
                    break;
                }
            }
        }
        
        return status;
    }
}
