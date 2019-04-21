/*
 * Copyright 2019 asanetargoss
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

package targoss.hardcorealchemy.test.suite;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import targoss.hardcorealchemy.test.api.ITestList;
import targoss.hardcorealchemy.test.api.ITestSuite;
import targoss.hardcorealchemy.test.api.TestList;
import targoss.hardcorealchemy.util.MiscVanilla;

public class TestTimeFuzz implements ITestSuite {

    @Override
    public ITestList getTests() {
        ITestList tests = new TestList();

        tests.put("server time fuzz disabled", this::checkServerFuzzDisabled);
        tests.put("world time fuzz disabled", this::checkWorldFuzzDisabled);
        tests.put("server time fuzz disabled function", this::checkServerFuzzDisabledFunction);
        tests.put("world time fuzz disabled function", this::checkWorldFuzzDisabledFunction);
        tests.put("server time fuzz enabled", this::checkServerFuzzEnabled);
        tests.put("world time fuzz enabled", this::checkWorldFuzzEnabled);
        tests.put("server time fuzz enabled function", this::checkServerFuzzEnabledFunction);
        tests.put("world time fuzz enabled function", this::checkWorldFuzzEnabledFunction);
        tests.put("server time fuzz re-disabled", this::checkServerFuzzDisabled);
        tests.put("world time fuzz re-disabled", this::checkWorldFuzzDisabled);
        tests.put("server time fuzz re-disabled function", this::checkServerFuzzDisabledFunction);
        tests.put("world time fuzz re-disabled function", this::checkWorldFuzzDisabledFunction);
        
        return tests;
    }
    
    public boolean checkServerFuzzDisabled(int fuzzChecks) {
        // Server time in milliseconds should either increase or stay the same
        // If there is an RNG in place, there will be great variation in the value
        long lastTime = MinecraftServer.getCurrentTimeMillis();
        for (int i = 0; i < fuzzChecks; i++) {
            long time = MinecraftServer.getCurrentTimeMillis();
            if (time < lastTime) {
                return false;
            }
            lastTime = time;
        }
        return true;
    }
    
    public boolean checkServerFuzzDisabled() {
        return checkServerFuzzDisabled(5);
    }
    
    public boolean checkWorldFuzzDisabled(int fuzzChecks, World world) {
        // World is not ticking, so its value should stay the same unless there's RNG
        long lastTime = world.getWorldTime();
        for (int i = 0; i < fuzzChecks; i++) {
            long time = world.getWorldTime();
            if (time != lastTime) {
                return false;
            }
            lastTime = time;
        }
        return true;
    }
    
    public boolean checkWorldFuzzDisabled() {
        return checkWorldFuzzDisabled(5, MiscVanilla.getWorld());
    }
    
    public boolean checkServerFuzzDisabledFunction() {
        return !MiscVanilla.isFuzzingTime(false);
    }

    public boolean checkWorldFuzzDisabledFunction() {
        return !MiscVanilla.isFuzzingTime(true) && !MiscVanilla.isFuzzingTime(false);
    }
    
    public boolean checkServerFuzzEnabled() {
        MiscVanilla.enableTimeFuzz(false);
        boolean fuzzDisabled = checkServerFuzzDisabled();
        MiscVanilla.disableTimeFuzz(false);
        return !fuzzDisabled;
    }
    
    public boolean checkWorldFuzzEnabled() {
        MiscVanilla.enableTimeFuzz(false);
        MiscVanilla.enableTimeFuzz(true);
        boolean fuzzDisabled = checkWorldFuzzDisabled();
        MiscVanilla.disableTimeFuzz(false);
        MiscVanilla.disableTimeFuzz(true);
        return !fuzzDisabled;
    }
    
    public boolean checkServerFuzzEnabledFunction() {
        MiscVanilla.enableTimeFuzz(false);
        boolean fuzzEnabled = !checkServerFuzzDisabledFunction();
        MiscVanilla.disableTimeFuzz(false);
        return fuzzEnabled;
    }
    
    public boolean checkWorldFuzzEnabledFunction() {
        MiscVanilla.enableTimeFuzz(true);
        MiscVanilla.enableTimeFuzz(false);
        boolean fuzzEnabled = !checkWorldFuzzDisabledFunction();
        MiscVanilla.disableTimeFuzz(true);
        MiscVanilla.disableTimeFuzz(false);
        return fuzzEnabled;
    }

}
