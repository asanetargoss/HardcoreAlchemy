package targoss.hardcorealchemy.test.suite;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import targoss.hardcorealchemy.test.HardcoreAlchemyTests;
import targoss.hardcorealchemy.test.api.ITestList;
import targoss.hardcorealchemy.test.api.ITestSuite;
import targoss.hardcorealchemy.test.api.TestList;

public class TestFakePlayer implements ITestSuite {
    @Override
    public ITestList getTests() {
        ITestList tests = new TestList();
        
        tests.put("check server reference", this::checkServerTestReference);
        tests.put("check overworld available", this::checkOverworldAvailable);
        
        return tests;
    }

    public boolean checkServerTestReference() {
        return HardcoreAlchemyTests.SERVER_REFERENCE != null && HardcoreAlchemyTests.SERVER_REFERENCE.get() != null;
    }

    public boolean checkOverworldAvailable() {
        MinecraftServer server = HardcoreAlchemyTests.SERVER_REFERENCE.get();
        WorldServer worldServer = server.worldServerForDimension(DimensionType.OVERWORLD.getId());
        return worldServer != null;
    }

}
