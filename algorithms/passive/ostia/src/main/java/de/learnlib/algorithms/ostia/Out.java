/* Copyright (C) 2013-2020 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithms.ostia;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author Aleksander Mendoza-Drosik
 */
class Out {

    @Nullable IntQueue str;

    Out(@Nullable IntQueue str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return String.valueOf(str);
    }
}
