/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.listener;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.event.EventSendChatMessage;
import targoss.hardcorealchemy.incantation.IncantationLookup;
import targoss.hardcorealchemy.incantation.IncantationParser;
import targoss.hardcorealchemy.incantation.IncantationParts;
import targoss.hardcorealchemy.incantation.IncantationParts.IncantationDefinition;
import targoss.hardcorealchemy.incantation.api.ISpell;
import targoss.hardcorealchemy.incantation.api.Incantation;
import targoss.hardcorealchemy.network.RequestIncantation;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.Chat.Type;

public class ListenerPlayerIncantation extends HardcoreAlchemyListener {
    protected static final Set<String> fillerCharacters = new HashSet<>();
    static {
        fillerCharacters.add(" ");
        fillerCharacters.add("\t");
        fillerCharacters.add("\n");
        fillerCharacters.add(",");
        fillerCharacters.add(";");
        fillerCharacters.add(".");
        fillerCharacters.add("\"");
        fillerCharacters.add("!");
        fillerCharacters.add("?");
    }
    
    /**
     * Step 1: Break up the player chat message into filler (whitespace, etc)
     * and words (continuous letters and numbers).
     * Words will later be candidates to become incantations.
     */
    protected static IncantationParts preParseIncantations(String message) {
        IncantationParts parts = new IncantationParts();
        
        String piece = "";
        IncantationParts.Type type = IncantationParts.Type.FILLER;
        for (int i = 0; i < message.length(); ++i) {
            char c = message.charAt(i);
            IncantationParts.Type nextType;
            if (fillerCharacters.contains("" + c)) {
                nextType = IncantationParts.Type.FILLER;
            } else {
                nextType = IncantationParts.Type.WORD;
            }
            
            if (nextType != type && !piece.isEmpty()) {
                switch (type) {
                case FILLER:
                    parts.addFiller(piece);
                    break;
                case WORD:
                    parts.addWord(piece);
                    break;
                case INCANTATION:
                    break;
                }
                piece = "";
            }
            
            type = nextType;
            piece += c;
        }
        
        if (!piece.isEmpty()) {
            switch (type) {
            case FILLER:
                parts.addFiller(piece);
                break;
            case WORD:
                parts.addWord(piece);
                break;
            case INCANTATION:
                break;
            }
            piece = "";
        }
        
        return parts;
    }
    
    protected static final IncantationLookup INCANTATION_LOOKUP = new IncantationLookup();
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        int pressedKey = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
        boolean reloadedResources = Keyboard.isKeyDown(61) && pressedKey == 20;
        if (reloadedResources) {
            INCANTATION_LOOKUP.clear();
        }
    }
    
    /**
     * This is where words get converted into incantations.
     * Later on, some of this functionality will move into IncantationParser,
     * so that incantations can do their own parsing.
     */
    protected static IncantationParts parseIncantations(IncantationParts preParts) {
        IncantationParts parts = new IncantationParts();
        IncantationParser parser = new IncantationParser();
        
        IncantationParts.Iterator iterator = preParts.iterator();
        while (iterator.hasNext()) {
            IncantationParts.Type preType = iterator.checkNextType();
            switch (preType) {
            case FILLER:
                String filler = iterator.nextFiller();
                parts.addFiller(filler);
                break;
            case WORD:
                String word = iterator.nextWord();
                Incantation incantation = INCANTATION_LOOKUP.get(word);
                if (incantation == null) {
                    parts.addWord(word);
                } else {
                    ISpell spell = incantation.getSpell(parser);
                    if (spell != null) {
                        parts.addIncantation(new IncantationDefinition(word, incantation, spell));
                    } else {
                        parts.addWord(word);
                    }
                }
                break;
            case INCANTATION:
                // Shouldn't happen
                break;
            }
        }
        
        return parts;
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onSendChatMessage(EventSendChatMessage event) {
        if (event.message == null || event.message.startsWith("/")) {
            return;
        }
        IncantationParts preParts = preParseIncantations(event.message);
        IncantationParts parts = parseIncantations(preParts);

        if (!parts.hasIncantation()) {
            return;
        }
        event.setCanceled(true);
        
        // Upon successful incantation parse, send result to the server
        HardcoreAlchemyCore.proxy.messenger.sendToServer(new RequestIncantation(parts));
    }
    
    protected static ITextComponent getMessageFromParts(IncantationParts parts) {
        TextComponentString message = new TextComponentString("");
        IncantationParts.Iterator iterator = parts.iterator();
        while (iterator.hasNext()) {
            switch (iterator.checkNextType()) {
            case FILLER:
                String filler = iterator.nextFiller();
                message.appendText(filler);
                break;
            case WORD:
                String word = iterator.nextWord();
                message.appendText(word);
                break;
            case INCANTATION:
                IncantationParts.IncantationDefinition incantation = iterator.nextIncantation();
                TextComponentString incantationComponent = new TextComponentString(incantation.displayString);
                incantationComponent.setStyle(Chat.Type.INCANTATION.style);
                message.appendSibling(incantationComponent);
                break;
            }
        }
        return message;
    }
    
    /**
     * A cooldown to prevent spamming.
     */
    protected static final int INCANTATION_COOLDOWN_TICKS = 10;
    /**
     * Max (top-level) castable spells.
     */
    protected static final int MAX_SPELLS_CAST_PER_MESSAGE = 10;
    
    public static void invokeSpells(EntityPlayerMP player, IncantationParts parts) {
        // Prevent player from casting spells too quickly
        int timeSinceIncantation = -1;
        ICapabilityMisc misc = player.getCapability(ProviderMisc.MISC_CAPABILITY, null);
        if (misc != null) {
            timeSinceIncantation = player.ticksExisted - misc.getLastIncantationTick();
        }
        if (timeSinceIncantation > 0 && timeSinceIncantation < INCANTATION_COOLDOWN_TICKS) {
            // Casting spells too quickly
            Chat.message(Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.incantation.too_fast"));
            return;
        }
        misc.setLastIncantationTick(player.ticksExisted);
        
        // Prepare the chat message to display before the spell
        int spellsToCast = 0;
        IncantationParts partsToExecute = new IncantationParts();
        IncantationParts.Iterator iterator = parts.iterator();
        while (iterator.hasNext()) {
            switch (iterator.checkNextType()) {
            case FILLER:
                String filler = iterator.nextFiller();
                partsToExecute.addFiller(filler);
                break;
            case WORD:
                String word = iterator.nextWord();
                partsToExecute.addWord(word);
                break;
            case INCANTATION:
                IncantationParts.IncantationDefinition incantation = iterator.nextIncantation();
                ISpell spell = incantation.spell;
                if (spellsToCast < MAX_SPELLS_CAST_PER_MESSAGE && spell.canInvoke(player)) {
                    partsToExecute.addIncantation(incantation);
                    ++spellsToCast;
                } else {
                    partsToExecute.addWord(incantation.displayString);
                }
                break;
            }
        }
        
        // Display fancy message (or less fancy if not successful)
        ITextComponent message = getMessageFromParts(partsToExecute);
        Chat.playerChatMessage(Type.DEFAULT, player, message);

        // Cast the spell(s)
        IncantationParts.Iterator executeIterator = partsToExecute.iterator();
        while (executeIterator.hasNext()) {
            switch (executeIterator.checkNextType()) {
            case FILLER:
                executeIterator.nextFiller();
                break;
            case WORD:
                executeIterator.nextWord();
                break;
            case INCANTATION:
                IncantationParts.IncantationDefinition incantation = executeIterator.nextIncantation();
                ISpell spell = incantation.spell;
                spell.invoke(player);
                break;
            }
        }
    }
}
