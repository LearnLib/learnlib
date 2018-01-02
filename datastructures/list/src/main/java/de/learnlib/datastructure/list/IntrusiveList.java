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
package de.learnlib.datastructure.list;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

/**
 * The head of the intrusive linked list for storing incoming transitions of a DT node.
 *
 * @param <T>
 *         element type
 *
 * @author Malte Isberner
 */
public class IntrusiveList<T extends IntrusiveListElem<T>> extends IntrusiveListElemImpl<T> implements Iterable<T> {

    public boolean isEmpty() {
        return next == null;
    }

    /**
     * Retrieves any block from the list. If the list is empty, {@code null} is returned.
     *
     * @return any block from the list, or {@code null} if the list is empty.
     */
    public T choose() {
        return next;
    }

    @Override
    public Iterator<T> iterator() {
        return new ListIterator(next);
    }

    private class ListIterator extends AbstractIterator<T> {

        private T cursor;

        ListIterator(T start) {
            this.cursor = start;
        }

        @Override
        protected T computeNext() {
            if (cursor == null) {
                return endOfData();
            }

            final T result = cursor;
            cursor = cursor.getNextElement();
            return result;
        }
    }
}
