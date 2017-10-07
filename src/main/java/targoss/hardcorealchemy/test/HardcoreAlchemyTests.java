package targoss.hardcorealchemy.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.test.api.ITestSuite;
import targoss.hardcorealchemy.test.api.TestSystem;
import targoss.hardcorealchemy.test.suite.TestFoodRot;

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
    
    @Override
    public Set<Class<? extends ITestSuite>> getTestSuites() {
        Set<Class<? extends ITestSuite>> testSuites = new HashSet<>();
        
        return testSuites;
    }
}
