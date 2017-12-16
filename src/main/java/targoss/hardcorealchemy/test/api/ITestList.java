package targoss.hardcorealchemy.test.api;

public interface ITestList extends Iterable<ITestList.TestEntry> {
    int size();
    
    void put(String name, Test test);
    
    /**
     * Only add if true.
     * Use this instead of if statements.
     * Later, the test framework can use this to
     * determine which tests exist but can't be run.
     */
    void putIf(String name, Test test, boolean shouldAdd);
    
    public static class TestEntry {
        private final String name;
        private final Test test;
        protected TestEntry (String name, Test test) {
            this.name = name;
            this.test = test;
        }
        
        public String getName() {
            return this.name;
        }
        
        public boolean evaluate() {
            return test.result();
        }
    }
}
