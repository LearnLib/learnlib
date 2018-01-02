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
package de.learnlib.algorithms.discriminationtree.hypothesis.vpda;

import de.learnlib.datastructure.list.IntrusiveList;

/**
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class TransList<I> extends IntrusiveList<AbstractHypTrans<I>> {

    public void add(AbstractHypTrans<I> trans) {
        assert !trans.isTree();
        trans.removeFromList();

        if (next != null) {
            trans.setNextElement(next);
            next.prev = trans;
        }
        trans.prev = this;
        next = trans;
    }

    public void addAll(TransList<I> list) {
        if (list.next != null) {
            AbstractHypTrans<I> last = list.next;
            while (last.getNextElement() != null) {
                last = last.getNextElement();
            }
            if (next != null) {
                next.prev = last;
            }
            last.setNextElement(next);
            list.next.prev = this;
            next = list.next;
        }
        list.next = null;
    }

    public AbstractHypTrans<I> chooseMinimal() {
        AbstractHypTrans<I> curr = next;
        AbstractHypTrans<I> shortest = curr;
        int shortestLen = shortest.getAccessSequence().length();

        curr = curr.getNextElement();
        while (curr != null) {
            int transLen = curr.getAccessSequence().length();
            if (transLen < shortestLen) {
                shortestLen = transLen;
                shortest = curr;
            }
            curr = curr.getNextElement();
        }

        return shortest;
    }

    public AbstractHypTrans<I> poll() {
        if (next == null) {
            return null;
        }
        AbstractHypTrans<I> result = next;
        next = result.getNextElement();
        if (result.getNextElement() != null) {
            result.getNextElement().prev = this;
        }

        result.prev = null;
        result.setNextElement(null);

        return result;
    }

    public int size() {
        AbstractHypTrans<I> curr = next;
        int i = 0;
        while (curr != null) {
            i++;
            curr = curr.getNextElement();
        }

        return i;
    }

}
