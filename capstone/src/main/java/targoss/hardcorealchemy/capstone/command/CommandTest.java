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

package targoss.hardcorealchemy.capstone.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import targoss.hardcorealchemy.capstone.test.HardcoreAlchemyTests;

public class CommandTest extends CommandBase {

    @Override
    public String getName() {
        return "hcatest";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "hcatest";
    }
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return !server.isDedicatedServer() || sender.canUseCommand(server.getOpPermissionLevel(), this.getName());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        /* Hardcore Alchemy development requires English,
         * and ambiguity in technical assistance should be avoided,
         * so no translations here.
         */
        sender.sendMessage(new TextComponentString("Running tests..."));
        HardcoreAlchemyTests.runAndLogTests();
        sender.sendMessage(new TextComponentString("Tests finished and logged to logs/fml-client-latest.log"));
    }
    
}
