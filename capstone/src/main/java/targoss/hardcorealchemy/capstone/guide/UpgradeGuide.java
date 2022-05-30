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

package targoss.hardcorealchemy.capstone.guide;

import amerifrance.guideapi.api.impl.Book;

public class UpgradeGuide {
    public String upgradeTo;
    public Book book;

    // Internal
    public BookBuilder bookBuilder;
    public BookBuilder.Result bookBuilderResult;
    
    public UpgradeGuide(BookBuilder bookBuilder) {
        this.bookBuilder = bookBuilder;
    }

    public static int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int n = Math.min(parts1.length, parts2.length);
        for (int i = 0; i < n; ++i) {
            String part1 = parts1[i];
            String part2 = parts2[i];
            boolean isInt1 = part1.matches("^\\d+$");
            boolean isInt2 = part2.matches("^\\d+$");
            if (isInt1 && isInt2) {
                int num1 = Integer.parseInt(part1);
                int num2 = Integer.parseInt(part2);
                if (num1 < num2) {
                    return -1;
                } else if (num1 > num2) {
                    return 1;
                }
            } else if (isInt1 != isInt2) {
                if (isInt2) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                int stringCompare = part1.compareTo(part2);
                if (stringCompare != 0) {
                    return stringCompare;
                }
            }
        }
        if (parts1.length < parts2.length) {
            return -1;
        } else if (parts1.length > parts2.length) {
            return 1;
        } else {
            return 0;
        }
    }
}
