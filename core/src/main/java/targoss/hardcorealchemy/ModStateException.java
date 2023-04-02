/*
 * Copyright 2017-2023 asanetargoss
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

package targoss.hardcorealchemy;

public class ModStateException extends RuntimeException {
    public ModStateException() {
        super();
    }

    public ModStateException(String paramString, Throwable paramThrowable, boolean paramBoolean1,
            boolean paramBoolean2) {
        super(paramString, paramThrowable, paramBoolean1, paramBoolean2);
    }

    public ModStateException(String paramString, Throwable paramThrowable) {
        super(paramString, paramThrowable);
    }

    public ModStateException(Throwable paramThrowable) {
        super(paramThrowable);
    }

    public ModStateException(String paramString) {
        super(paramString);
    }
}
