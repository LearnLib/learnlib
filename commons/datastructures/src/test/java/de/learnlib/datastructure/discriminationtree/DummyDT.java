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
package de.learnlib.datastructure.discriminationtree;

import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import net.automatalib.word.Word;

final class DummyDT {

    static final BinaryDTree<Integer, Character> DT;
    static final AbstractWordBasedDTNode<Integer, Boolean, Character> INNER_1, INNER_2, LEAF_1, LEAF_2, LEAF_3;

    static {
        /*
        Construct the following DT:
                 1
        (false) / \ (true)
               a   2
          (false) / \ (true)
                 b   c
         */

        DT = new BinaryDTree<>(null);

        INNER_1 = DT.getRoot();

        AbstractWordBasedDTNode<Integer, Boolean, Character>.SplitResult sr1 =
                INNER_1.split(Word.fromLetter(1), false, true);

        LEAF_1 = sr1.nodeOld;
        INNER_2 = sr1.nodeNew;

        LEAF_1.setData('a');
        AbstractWordBasedDTNode<Integer, Boolean, Character>.SplitResult sr2 =
                INNER_2.split(Word.fromLetter(2), false, true);

        LEAF_2 = sr2.nodeOld;
        LEAF_3 = sr2.nodeNew;

        LEAF_2.setData('b');
        LEAF_3.setData('c');
    }

    private DummyDT() {
        // prevent instantiation
    }
}
