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

package targoss.hardcorealchemy.capability.research;

import java.util.Set;

import targoss.hardcorealchemy.research.KnowledgeFact;

/**
 * NOTE: This doesn't sync yet. It may be desirable to sync it in the future.
 */
public interface ICapabilityResearch {
    Set<KnowledgeFact> getKnowledgeFacts();
    void setKnowledgeFacts(Set<KnowledgeFact> knowledgeFacts);
}
