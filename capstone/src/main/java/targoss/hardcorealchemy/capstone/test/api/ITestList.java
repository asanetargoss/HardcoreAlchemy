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
