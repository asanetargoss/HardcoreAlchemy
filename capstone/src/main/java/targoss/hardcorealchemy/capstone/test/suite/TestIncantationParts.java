/*
 * Copyright 2017-2026 asanetargoss
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

package targoss.hardcorealchemy.capstone.test.suite;

import targoss.hardcorealchemy.capstone.test.api.ITestList;
import targoss.hardcorealchemy.capstone.test.api.ITestSuite;
import targoss.hardcorealchemy.capstone.test.api.TestList;
import targoss.hardcorealchemy.incantation.IncantationParts;
import targoss.hardcorealchemy.incantation.IncantationParts.IncantationDefinition;
import targoss.hardcorealchemy.incantation.Incantations;
import targoss.hardcorealchemy.util.StringUtil;

public class TestIncantationParts implements ITestSuite {

    @Override
    public ITestList getTests() {
        ITestList tests = new TestList();
        
        tests.put("string util trim before", this::testStringUtilTrimBefore);
        tests.put("string util trim after", this::testStringUtilTrimAfter);
        tests.put("empty", this::testEmpty);
        tests.put("empty is invalid", this::testEmptyInvalid);
        tests.put("simple valid", this::testSimpleIncantationValid);
        tests.put("bad incantation display string", this::testBadIncantationDisplayString);
        tests.put("bad word", this::testBadWord);
        tests.put("bad filler", this::testBadFiller);
        tests.put("missing incantation element", this::testElementsMissingIncantation);
        tests.put("missing word element", this::testElementsMissingWord);
        tests.put("missing filler element", this::testElementsMissingFiller);
        tests.put("extra incantation element", this::testElementsExtraIncantation);
        tests.put("extra word element", this::testElementsExtraWord);
        tests.put("extra filler element", this::testElementsExtraFiller);
        tests.put("missing incantation", this::testIncantationMissing);
        tests.put("iterator", this::testIncantationIterator);
        tests.put("reverse iterator", this::testIncantationReverseIterator);
        tests.put("remove incantation before", this::testRemoveIncantationBefore);
        tests.put("remove incantation after", this::testRemoveIncantationAfter);
        tests.put("trim incantation before", this::testTrimIncantationBefore);
        tests.put("trim incantation after", this::testTrimIncantationAfter);
        tests.put("remove word before", this::testRemoveWordBefore);
        tests.put("remove word after", this::testRemoveWordAfter);
        tests.put("trim word before", this::testTrimWordBefore);
        tests.put("trim word after", this::testTrimWordAfter);
        tests.put("remove filler before", this::testRemoveFillerBefore);
        tests.put("remove filler after", this::testRemoveFillerAfter);
        tests.put("trim filler before", this::testTrimFillerBefore);
        tests.put("trim filler after", this::testTrimFillerAfter);
        
        return tests;
    }

    public boolean testStringUtilTrimBefore() {
        String padded = "  test123  ";
        String trimmed = StringUtil.trimBefore(padded);
        return trimmed.equals("test123  ");
    }
    
    public boolean testStringUtilTrimAfter() {
        String padded = "  foo456  ";
        String trimmed = StringUtil.trimAfter(padded);
        return trimmed.equals("  foo456");
    }
    
    public boolean testEmpty() {
        IncantationParts parts = new IncantationParts();
        return parts.isEmpty();
    }
    
    public boolean testEmptyInvalid() {
        IncantationParts parts = new IncantationParts();
        return !parts.isValid();
    }
    
    public boolean testSimpleIncantationValid() {
        IncantationDefinition incantation_in = new IncantationDefinition("my_incantation", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(incantation_in);
        if (parts.isEmpty()) { return false; }
        return parts.isValid();
    }
    
    public boolean testBadIncantationDisplayString() {
        IncantationDefinition incantation_in = new IncantationDefinition("§totally normal incantation", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(incantation_in);
        return !parts.isValid();
    }
    
    public boolean testBadWord() {
        IncantationDefinition incantation_in = new IncantationDefinition("yes_really_normal_incantation", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(incantation_in);
        parts.addWord("§hi");
        return !parts.isValid();
    }

    public boolean testBadFiller() {
        IncantationDefinition incantation_in = new IncantationDefinition("actually_normal", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(incantation_in);
        parts.addFiller("§glamourous");
        return !parts.isValid();
    }
    
    @SuppressWarnings("deprecation")
    public boolean testElementsMissingIncantation() {
        IncantationParts parts = new IncantationParts();
        parts.addWord("hello");
        parts.types.add(IncantationParts.Type.INCANTATION);
        return !parts.isValid();
    }

    @SuppressWarnings("deprecation")
    public boolean testElementsMissingWord() {
        IncantationParts parts = new IncantationParts();
        IncantationDefinition incantation = new IncantationDefinition("scambled", Incantations.INCANTATION_CREATE_SLATE, null);
        parts.addIncantation(incantation);
        parts.types.add(IncantationParts.Type.WORD);
        return !parts.isValid();
    }

    @SuppressWarnings("deprecation")
    public boolean testElementsMissingFiller() {
        IncantationParts parts = new IncantationParts();
        IncantationDefinition incantation = new IncantationDefinition("doomed", Incantations.INCANTATION_CREATE_SLATE, null);
        parts.addIncantation(incantation);
        parts.types.add(IncantationParts.Type.FILLER);
        return !parts.isValid();
    }

    @SuppressWarnings("deprecation")
    public boolean testElementsExtraIncantation() {
        IncantationParts parts = new IncantationParts();
        IncantationDefinition incantation = new IncantationDefinition("busted", Incantations.INCANTATION_CREATE_SLATE, null);
        parts.addIncantation(incantation);
        IncantationDefinition incantation2 = new IncantationDefinition("canceled", Incantations.INCANTATION_CREATE_SLATE, null);
        parts.incantations.add(incantation2);
        return !parts.isValid();
    }

    @SuppressWarnings("deprecation")
    public boolean testElementsExtraWord() {
        IncantationParts parts = new IncantationParts();
        IncantationDefinition incantation = new IncantationDefinition("oopsied", Incantations.INCANTATION_CREATE_SLATE, null);
        parts.addIncantation(incantation);
        parts.words.add("daisies");
        return !parts.isValid();
    }

    @SuppressWarnings("deprecation")
    public boolean testElementsExtraFiller() {
        IncantationParts parts = new IncantationParts();
        IncantationDefinition incantation = new IncantationDefinition("spotted", Incantations.INCANTATION_CREATE_SLATE, null);
        parts.addIncantation(incantation);
        parts.fillers.add("!!!");
        return !parts.isValid();
    }
    
    public boolean testIncantationMissing() {
        String fake = "fake_incantation";
        String filler = "filler";
        IncantationParts parts = new IncantationParts();
        parts.addWord(fake);
        parts.addFiller(filler);
        if (parts.isEmpty()) { return false; }
        return !parts.isValid();
    }
    
    public boolean testIncantationIterator() {
        IncantationDefinition incantation_in = new IncantationDefinition("my_incantation", Incantations.INCANTATION_CREATE_SLATE, null);
        String filler_in = " ";
        String word_in = "hello";
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(incantation_in);
        parts.addFiller(filler_in);
        parts.addWord(word_in);
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        
        IncantationParts.Iterator it = parts.iterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (incantation != incantation_in) { return false; }
        if (it.checkNextType() != IncantationParts.Type.FILLER) { return false; }
        String filler = it.nextFiller();
        if (filler != filler_in) { return false; }
        if (it.checkNextType() != IncantationParts.Type.WORD) { return false; }
        String word = it.nextWord();
        if (word != word_in) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testIncantationReverseIterator() {
        IncantationDefinition incantation_in = new IncantationDefinition("my_incantation", Incantations.INCANTATION_CREATE_SLATE, null);
        String filler_in = " ";
        String word_in = "hello";
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(incantation_in);
        parts.addFiller(filler_in);
        parts.addWord(word_in);
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        
        IncantationParts.ReverseIterator it = parts.reverseIterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.WORD) { return false; }
        String word = it.nextWord();
        if (word != word_in) { return false; }
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.FILLER) { return false; }
        String filler = it.nextFiller();
        if (filler != filler_in) { return false; }
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (incantation != incantation_in) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testRemoveIncantationBefore() {
        IncantationDefinition blank = new IncantationDefinition("  ", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationDefinition not_blank = new IncantationDefinition("ikwid", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(blank);
        parts.addIncantation(not_blank);
        parts.trim();
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        IncantationParts.Iterator it = parts.iterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (!incantation.displayString.equals(not_blank.displayString)) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testRemoveIncantationAfter() {
        IncantationDefinition not_blank = new IncantationDefinition("ikwid", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationDefinition blank = new IncantationDefinition("  ", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(not_blank);
        parts.addIncantation(blank);
        parts.trim();
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        IncantationParts.ReverseIterator it = parts.reverseIterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (!incantation.displayString.equals(not_blank.displayString)) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testTrimIncantationBefore() {
        IncantationDefinition padded_before = new IncantationDefinition("  padded  ", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationDefinition after = new IncantationDefinition("  after", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(padded_before);
        parts.addIncantation(after);
        parts.trim();
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        IncantationParts.Iterator it = parts.iterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (!incantation.displayString.equals("padded  ")) { return false; }
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation2 = it.nextIncantation();
        if (!incantation2.displayString.equals("  after")) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testTrimIncantationAfter() {
        IncantationDefinition before = new IncantationDefinition("before  ", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationDefinition padded_after = new IncantationDefinition("  padded  ", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(before);
        parts.addIncantation(padded_after);
        parts.trim();
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        IncantationParts.Iterator it = parts.iterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (!incantation.displayString.equals("before  ")) { return false; }
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation2 = it.nextIncantation();
        if (!incantation2.displayString.equals("  padded")) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testRemoveWordBefore() {
        String blank = "  ";
        IncantationDefinition not_blank = new IncantationDefinition("abracadabra", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationParts parts = new IncantationParts();
        parts.addWord(blank);
        parts.addIncantation(not_blank);
        parts.trim();
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        IncantationParts.Iterator it = parts.iterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (!incantation.displayString.equals(not_blank.displayString)) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testRemoveWordAfter() {
        IncantationDefinition not_blank = new IncantationDefinition("abracadabra", Incantations.INCANTATION_CREATE_SLATE, null);
        String blank = "  ";
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(not_blank);
        parts.addWord(blank);
        parts.trim();
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        IncantationParts.Iterator it = parts.iterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (!incantation.displayString.equals(not_blank.displayString)) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testTrimWordBefore() {
        String padded_before = "  padded  ";
        IncantationDefinition after = new IncantationDefinition("  after", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationParts parts = new IncantationParts();
        parts.addWord(padded_before);
        parts.addIncantation(after);
        parts.trim();
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        IncantationParts.Iterator it = parts.iterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.WORD){ return false; }
        String word = it.nextWord();
        if (!word.equals("padded  ")) { return false; }
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (!incantation.displayString.equals("  after")) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testTrimWordAfter() {
        IncantationDefinition before = new IncantationDefinition("before  ", Incantations.INCANTATION_CREATE_SLATE, null);
        String padded_after = "  padded  ";
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(before);
        parts.addWord(padded_after);
        parts.trim();
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        IncantationParts.Iterator it = parts.iterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION){ return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (!incantation.displayString.equals("before  ")) { return false; }
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.WORD) { return false; }
        String word = it.nextWord();
        if (!word.equals("  padded")) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testRemoveFillerBefore() {
        String blank = "  ";
        IncantationDefinition not_blank = new IncantationDefinition("pondering", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationParts parts = new IncantationParts();
        parts.addFiller(blank);
        parts.addIncantation(not_blank);
        parts.trim();
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        IncantationParts.Iterator it = parts.iterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (!incantation.displayString.equals(not_blank.displayString)) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testRemoveFillerAfter() {
        IncantationDefinition not_blank = new IncantationDefinition("pondering", Incantations.INCANTATION_CREATE_SLATE, null);
        String blank = "  ";
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(not_blank);
        parts.addFiller(blank);
        parts.trim();
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        IncantationParts.Iterator it = parts.iterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (!incantation.displayString.equals(not_blank.displayString)) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testTrimFillerBefore() {
        String padded_before = "  padded  ";
        IncantationDefinition after = new IncantationDefinition("  after", Incantations.INCANTATION_CREATE_SLATE, null);
        IncantationParts parts = new IncantationParts();
        parts.addFiller(padded_before);
        parts.addIncantation(after);
        parts.trim();
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        IncantationParts.Iterator it = parts.iterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.FILLER){ return false; }
        String word = it.nextFiller();
        if (!word.equals("padded  ")) { return false; }
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION) { return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (!incantation.displayString.equals("  after")) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
    
    public boolean testTrimFillerAfter() {
        IncantationDefinition before = new IncantationDefinition("before  ", Incantations.INCANTATION_CREATE_SLATE, null);
        String padded_after = "  padded  ";
        IncantationParts parts = new IncantationParts();
        parts.addIncantation(before);
        parts.addFiller(padded_after);
        parts.trim();
        if (!parts.isValid()) { return false; }
        if (parts.isEmpty()) { return false; }
        IncantationParts.Iterator it = parts.iterator();
        
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.INCANTATION){ return false; }
        IncantationDefinition incantation = it.nextIncantation();
        if (!incantation.displayString.equals("before  ")) { return false; }
        if (!it.hasNext()) { return false; }
        if (it.checkNextType() != IncantationParts.Type.FILLER) { return false; }
        String word = it.nextFiller();
        if (!word.equals("  padded")) { return false; }
        
        if (it.hasNext()) { return false; }
        return true;
    }
}
