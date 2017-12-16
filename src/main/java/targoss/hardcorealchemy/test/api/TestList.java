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
