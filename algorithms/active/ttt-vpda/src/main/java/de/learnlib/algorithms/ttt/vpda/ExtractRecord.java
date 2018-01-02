/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.algorithms.ttt.vpda;

import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.DTNode;

/**
 * Data structure required during an extract operation. The latter basically works by copying nodes that are required in
 * the extracted subtree, and this data structure is required to associate original nodes with their extracted copies.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
final class ExtractRecord<I> {

    public final DTNode<I> original;

    public final DTNode<I> extracted;

    ExtractRecord(DTNode<I> original, DTNode<I> extracted) {
        this.original = original;
        this.extracted = extracted;
    }

}
