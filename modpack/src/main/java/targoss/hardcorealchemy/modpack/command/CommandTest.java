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

package targoss.hardcorealchemy.modpack.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import targoss.hardcorealchemy.modpack.test.HardcoreAlchemyTests;

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
        HardcoreAlchemyTests tests = new HardcoreAlchemyTests();
        /* Hardcore Alchemy development requires English,
         * and ambiguity in technical assistance should be avoided,
         * so no translations here.
         */
        sender.sendMessage(new TextComponentString("Running tests..."));
        tests.runAndLogTests();
        sender.sendMessage(new TextComponentString("Tests finished and logged to logs/fml-client-latest.log"));
    }
    
}
