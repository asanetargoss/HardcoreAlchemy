package targoss.hardcorealchemy.test.api;

import java.util.ArrayList;
import java.util.List;

import targoss.hardcorealchemy.test.api.ITestList.TestEntry;

public class TestList extends ArrayList<ITestList.TestEntry> implements ITestList {
    public void put(String name, Test test) {
        this.add(new TestEntry(name, test));
    }
}
