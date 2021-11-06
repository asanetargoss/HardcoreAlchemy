/*
 * Copyright 2021 asanetargoss
 * 
 * This file is part of the Hardcore Alchemy capstone mod.
 * 
 * The Hardcore Alchemy capstone mod is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3 of the
 * License.
 * 
 * The Hardcore Alchemy capstone mod is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Hardcore Alchemy capstone mod. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.modpack.guide;

import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.modpack.HardcoreAlchemyModpack;
import targoss.hardcorealchemy.modpack.registrar.RegistrarUpgradeGuide;

public class HCAUpgradeGuides {
    public static RegistrarUpgradeGuide UPGRADE_GUIDES = new RegistrarUpgradeGuide("upgrade_guides", HardcoreAlchemy.MOD_ID, HardcoreAlchemyModpack.LOGGER);

    static {
        UPGRADE_GUIDES.add("0.5.0", new UpgradeGuide(new BookBuilder()
                .addCategory(new BookBuilder.Category().setId("gameplay_tips").setItemId("sign")
                        .addEntry("earlygame", "sign", "1", "2", "3", "4", "5")
                        .addEntry("final_thoughts", "sign", "1")
                        .build())
                ));
        UPGRADE_GUIDES.add("0.6.0", new UpgradeGuide(new BookBuilder()
                .addCategory(new BookBuilder.Category().setId("backup").setItemId("sign")
                        .addEntry("backup", "sign", "1", "2", "3")
                        .build())
                .addCategory(new BookBuilder.Category().setId("gameplay_tips").setItemId("sign")
                        .addEntry("midgame", "embers:codex", "1", "2", "3", "4")
                        .build())
                ));
        UPGRADE_GUIDES.add("0.6.1", new UpgradeGuide(new BookBuilder()
                .addCategory(new BookBuilder.Category().setId("gameplay_tips").setItemId("sign")
                        .addEntry("earlygame", "sign", "1")
                        .build())
                ));
    }
    
}
