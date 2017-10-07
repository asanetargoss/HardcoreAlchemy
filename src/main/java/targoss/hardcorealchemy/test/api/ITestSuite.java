package targoss.hardcorealchemy.test.api;

import java.util.Map;

public interface ITestSuite {
    //TODO: Create Test List object and have that as an output instead (each containing information about a test)
    Map<String, Test> getTests();
}
