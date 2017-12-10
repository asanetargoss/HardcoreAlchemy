package targoss.hardcorealchemy.test.api;

public interface ITestList extends Iterable<ITestList.TestEntry> {
    int size();
    
    void put(String name, Test test);
    
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
