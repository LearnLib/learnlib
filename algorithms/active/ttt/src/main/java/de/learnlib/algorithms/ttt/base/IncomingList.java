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
 * The head of the intrusive linked list for storing incoming transitions of a DT node.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class IncomingList<I, D> extends IntrusiveList<TTTTransition<I, D>> {

    public void insertIncoming(TTTTransition<I, D> transition) {
        transition.removeFromList();

        transition.setNextElement(next);
        transition.prevIncoming = this;
        if (next != null) {
            next.prevIncoming = transition;
        }
        this.next = transition;
    }

    public void insertAllIncoming(IncomingList<I, D> list) {
        insertAllIncoming(list.next);
        list.next = null;
    }

    public void insertAllIncoming(TTTTransition<I, D> firstTransition) {
        if (firstTransition == null) {
            return;
        }
        firstTransition.prevIncoming.setNextElement(null);

        if (next == null) {
            next = firstTransition;
            firstTransition.prevIncoming = this;
        } else {
            TTTTransition<I, D> oldNext = next;
            next = firstTransition;
            firstTransition.prevIncoming = this;
            TTTTransition<I, D> last = firstTransition;

            while (last.getNextElement() != null) {
                last = last.getNextElement();
            }

            last.setNextElement(oldNext);
            oldNext.prevIncoming = last;
        }
    }

    public TTTTransition<I, D> poll() {
        TTTTransition<I, D> result = next;
        if (result != null) {
            this.next = result.getNextElement();
            if (this.next != null) {
                this.next.prevIncoming = this;
            }
            result.prevIncoming = null;
            result.setNextElement(null);
        }
        return result;
    }
}
