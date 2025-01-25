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
package de.learnlib.algorithm.aaar.abstraction;

import net.automatalib.word.Word;

class Node {

    static class InnerNode<CI, D> extends Node {

        final Word<CI> prefix;
        final Word<CI> suffix;
        final D out;

        Node equalsNext;
        Node otherNext;

        InnerNode(Word<CI> prefix, Word<CI> suffix, D out, Node equalsNext, Node otherNext) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.out = out;
            this.equalsNext = equalsNext;
            this.otherNext = otherNext;
        }
    }

    static class Leaf<AI, CI> extends Node {

        final AI abs;
        final CI rep;

        Leaf(AI abs, CI rep) {
            this.abs = abs;
            this.rep = rep;
        }
    }
}
