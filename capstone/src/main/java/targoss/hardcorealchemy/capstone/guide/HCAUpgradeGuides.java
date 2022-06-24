/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.capstone.guide;

import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.capstone.HardcoreAlchemyCapstone;
import targoss.hardcorealchemy.capstone.registrar.RegistrarUpgradeGuide;

public class HCAUpgradeGuides {
    public static RegistrarUpgradeGuide UPGRADE_GUIDES = new RegistrarUpgradeGuide("upgrade_guides", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyCapstone.LOGGER);

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
        UPGRADE_GUIDES.add("0.7.2", new UpgradeGuide(new BookBuilder()
                .addCategory(new BookBuilder.Category().setId("gameplay_tips").setItemId("sign")
                        .addEntry("earlygame", "sign", "1", "2")
                        .addEntry("midgame", "sign", "1", "2", "3")
                        .build())
                ));
        UPGRADE_GUIDES.add("0.8.1", new UpgradeGuide(new BookBuilder()
                .addCategory(new BookBuilder.Category().setId("gameplay_tips").setItemId("sign")
                        .addEntry("earlygame", "sign", "1", "2")
                        .build())
                ));
    }
    
}
