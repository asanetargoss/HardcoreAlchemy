package targoss.hardcorealchemy.tweaks.incantation;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.incantation.api.IIncantationParser;
import targoss.hardcorealchemy.incantation.api.ISpell;
import targoss.hardcorealchemy.incantation.api.Incantation;

public class IncantationFlintAndSteel extends Incantation {
    protected final ITextComponent NAME = new TextComponentTranslation(HardcoreAlchemyCore.MOD_ID + ".incantation.flint_and_steel.name");
    protected final ITextComponent COMMAND = new TextComponentTranslation(HardcoreAlchemyCore.MOD_ID + ".incantation.flint_and_steel.command");

    @Override
    public ITextComponent getName() {
        return NAME;
    }

    @Override
    public ITextComponent getCommand() {
        return COMMAND;
    }

    @Override
    public ISpell getSpell(IIncantationParser parser) {
        return new SpellFlintAndSteel();
    }

    @Override
    public void spellToBytes(ByteBuf buf, ISpell spell) {}

    @Override
    public ISpell spellFromBytes(ByteBuf buf) {
        return new SpellFlintAndSteel();
    }

}
