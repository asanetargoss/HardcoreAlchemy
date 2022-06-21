/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.research;

import static targoss.hardcorealchemy.research.Studies.KNOWLEDGE_FACTS;

import targoss.hardcorealchemy.research.KnowledgeFact;

public class Studies {
    public static final KnowledgeFact FACT_MAGIC_INHIBITION_WARNING = KNOWLEDGE_FACTS.add("magic_inhibition_warning", new KnowledgeFact(false));
    public static final KnowledgeFact FACT_DIG_DIRT_WARNING = KNOWLEDGE_FACTS.add("dig_dirt_warning", new KnowledgeFact(false));
}
