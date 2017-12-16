package targoss.hardcorealchemy.test;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.test.api.ITestSuite;
import targoss.hardcorealchemy.test.api.TestSystem;
import targoss.hardcorealchemy.test.suite.TestFakePlayer;
import targoss.hardcorealchemy.test.suite.TestFoodRot;

/**
 * Server-side tests for Hardcore Alchemy
 */
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
        
        testSuites.add(TestFakePlayer.class);
        testSuites.add(TestFoodRot.class);
        
        return testSuites;
    }
    
    public static WeakReference<MinecraftServer> SERVER_REFERENCE = null;
    
    public static void setServerForEvent(FMLServerStartingEvent event) {
        SERVER_REFERENCE = new WeakReference(event.getServer());
    }
    
    public static void setServerForEvent(FMLServerStoppingEvent event) {
        SERVER_REFERENCE = null;
    }
}
