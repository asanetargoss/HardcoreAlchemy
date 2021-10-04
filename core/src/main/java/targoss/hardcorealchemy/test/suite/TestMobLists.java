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

package targoss.hardcorealchemy.test.suite;

import java.util.Collection;
import java.util.Map;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.listener.ListenerSmallTweaks;
import targoss.hardcorealchemy.test.HardcoreAlchemyTests;
import targoss.hardcorealchemy.test.api.ITestList;
import targoss.hardcorealchemy.test.api.ITestSuite;
import targoss.hardcorealchemy.test.api.TestList;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.MobLists;

public class TestMobLists implements ITestSuite {
    
    private Map<String, ModContainer> indexedModList;
    
    public TestMobLists() {
        this.indexedModList = Loader.instance().getIndexedModList();
    }

    @Override
    public ITestList getTests() {
        ITestList tests = new TestList();
        
        tests.put("Test entity domain getter: minecraft", this::testEntityDomainVanilla);
        tests.put("Test entity domain getter: somemod", this::testEntityDomainMod);
        tests.put("list integrity: bosses", this::checkBossList);
        tests.put("list integrity: non-mobs", this::checkNonMobList);
        tests.put("list integrity: humans", this::checkHumanList);
        tests.put("list integrity: passive mobs", this::checkPassiveMobList);
        tests.put("list integrity: tame-ables", this::checkTameableList);
        tests.put("list integrity: night mobs", this::checkNightMobList);
        tests.put("list integrity: nether mobs", this::checkNetherMobList);
        
        boolean adInferos = Loader.instance().getIndexedModList().containsKey(ModState.ADINFEROS_ID);
        
        tests.putIf("test zombie reinforcements (Ad Inferos present)", this::testZombieReinforcements, adInferos);
        tests.putIf("test obsidian sheepman reinforcements", this::testSheepmanReinforcements, adInferos);
        
        return tests;
    }
    
    public String getEntityDomain(String entityName) {
        int separator = entityName.indexOf('.');
        if (separator == -1) {
            return "minecraft";
        }
        
        return entityName.substring(0, separator);
    }
    
    public boolean testEntityDomainVanilla() {
        return getEntityDomain("SomeVanillaEntity").equals("minecraft");
    }
    
    public boolean testEntityDomainMod() {
        return getEntityDomain("somemod.SomeModEntity").equals("somemod");
    }
    
    public boolean checkEntityList(Collection<String> entityList) {
        for (String entityName : entityList) {
            String domain = getEntityDomain(entityName);
            if (!domain.equals("minecraft") && !indexedModList.containsKey(domain)) {
                continue;
            }
            if (!EntityList.isStringValidEntityName(entityName)) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean checkBossList() {
        return checkEntityList(MobLists.getBosses());
    }
    
    public boolean checkNonMobList() {
        return checkEntityList(MobLists.getNonMobs());
    }
    
    public boolean checkHumanList() {
        return checkEntityList(MobLists.getHumans());
    }
    
    public boolean checkPassiveMobList() {
        return checkEntityList(MobLists.getPassiveMobs());
    }
    
    public boolean checkTameableList() {
        return checkEntityList(MobLists.getEntityTameables());
    }
    
    public boolean checkNightMobList() {
        return checkEntityList(MobLists.getNightMobs());
    }
    
    public boolean checkNetherMobList() {
        return checkEntityList(MobLists.getNetherMobs());
    }
    
    public boolean reinforcementAllowed(String zombieEntityName) {
        EntityZombie zombie = (EntityZombie)EntityUtil.createEntity(EntityList.NAME_TO_CLASS.get(zombieEntityName));
        
        ZombieEvent.SummonAidEvent event = new ZombieEvent.SummonAidEvent(zombie, zombie.world, 0, 0, 0, null, zombie.getEntityAttribute(EntityZombie.SPAWN_REINFORCEMENTS_CHANCE).getAttributeValue());
        ListenerSmallTweaks listener = new ListenerSmallTweaks();
        listener.setConfigs(HardcoreAlchemyTests.DEFAULT_CONFIGS);
        listener.onReinforceObsidianSheepman(event);
        
        return event.getResult() != Result.DENY;
    }
    
    public boolean testZombieReinforcements() {
        boolean result = false;
        try {
            result = reinforcementAllowed("Zombie");
        }
        catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        
        return result;
    }
    
    public boolean testSheepmanReinforcements() {
        boolean result = false;
        try {
            result = !reinforcementAllowed(ModState.ADINFEROS_ID + ".ObsidianSheepman");
        }
        catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        
        return result;
    }
}
