/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.algorithms.ttt.vpa;

import de.learnlib.algorithms.discriminationtree.hypothesis.vpa.ContextPair;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpa.HypLoc;

/**
 * @param <I>
 *         input symbol type
 */
final class OutputInconsistency<I> {

    public final HypLoc<I> location;

    public final ContextPair<I> discriminator;

    public final boolean expectedOut;

    OutputInconsistency(HypLoc<I> location, ContextPair<I> discriminator, boolean expectedOut) {
        this.location = location;
        this.discriminator = discriminator;
        this.expectedOut = expectedOut;
    }

    public int totalLength() {
        return location.getAccessSequence().length() + discriminator.getLength();
    }
}
