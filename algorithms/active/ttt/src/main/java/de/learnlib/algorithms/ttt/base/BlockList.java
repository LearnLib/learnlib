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
package de.learnlib.algorithms.ttt.base;

import de.learnlib.datastructure.list.IntrusiveList;

/**
 * A list for storing blocks (identified by their root {@link AbstractBaseDTNode}s). The list is implemented as a
 * singly-linked list, and allows O(1) insertion and removal of elements.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class BlockList<I, D> extends IntrusiveList<AbstractBaseDTNode<I, D>> {

    /**
     * Inserts a block into the list. Currently, the block is inserted at the head of the list. However, callers should
     * not rely on this.
     *
     * @param blockRoot
     *         the root node of the block to be inserted
     */
    public void insertBlock(AbstractBaseDTNode<I, D> blockRoot) {
        blockRoot.removeFromBlockList();

        blockRoot.setNextElement(next);
        if (getNextElement() != null) {
            next.setPrevElement(blockRoot);
        }
        blockRoot.setPrevElement(this);
        next = blockRoot;
    }

}
