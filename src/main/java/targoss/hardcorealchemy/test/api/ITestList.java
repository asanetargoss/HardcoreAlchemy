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
