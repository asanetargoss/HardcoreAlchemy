package targoss.hardcorealchemy.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.test.HardcoreAlchemyTests;

public class CommandTest extends CommandBase {

    @Override
    public String getCommandName() {
        return "hcatest";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "hcatest";
    }
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return !server.isDedicatedServer() || sender.canCommandSenderUseCommand(server.getOpPermissionLevel(), this.getCommandName());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        HardcoreAlchemyTests tests = new HardcoreAlchemyTests();
        /* Hardcore Alchemy development requires English,
         * and ambiguity in technical assistance should be avoided,
         * so no translations here.
         */
        sender.addChatMessage(new TextComponentString("Running tests..."));
        tests.runAndLogTests();
        sender.addChatMessage(new TextComponentString("Tests finished and logged to logs/fml-client-latest.log"));
    }
    
}
