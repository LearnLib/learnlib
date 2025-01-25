/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.algorithm.ttt.vpa;

import de.learnlib.algorithm.observationpack.vpa.hypothesis.DTNode;

/**
 * A global splitter. In addition to the information stored in a (local) {@link Splitter}, this class also stores the
 * block the local splitter applies to.
 *
 * @param <I>
 *         input symbol type
 */
final class GlobalSplitter<I> {

    public final Splitter<I> localSplitter;

    public final DTNode<I> blockRoot;

    GlobalSplitter(DTNode<I> blockRoot, Splitter<I> localSplitter) {
        this.blockRoot = blockRoot;
        this.localSplitter = localSplitter;
    }
}
