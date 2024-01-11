/* Copyright (C) 2013-2024 TU Dortmund University
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
import java.util.NoSuchElementException;

import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The head of the intrusive linked list for storing incoming transitions of a DT node.
 *
 * @param <T>
 *         element type
 */
public class IntrusiveList<T extends IntrusiveListElem<T>> extends IntrusiveListElemImpl<T> implements Iterable<T> {

    @EnsuresNonNullIf(expression = "next", result = false)
    public boolean isEmpty() {
        return next == null;
    }

    /**
     * Retrieves any block from the list. If the list is empty, {@code null} is returned.
     *
     * @return any block from the list, or {@code null} if the list is empty.
     */
    public @Nullable T choose() {
        return next;
    }

    public int size() {
        T curr = next;
        int i = 0;
        while (curr != null) {
            i++;
            curr = curr.getNextElement();
        }

        return i;
    }

    @Override
    public Iterator<T> iterator() {
        return new ListIterator(next);
    }

    private class ListIterator implements Iterator<T> {

        private @Nullable T cursor;

        ListIterator(@Nullable T start) {
            this.cursor = start;
        }

        @Override
        public boolean hasNext() {
            return cursor != null;
        }

        @Override
        public T next() {
            if (cursor == null) {
                throw new NoSuchElementException();
            }

            final T result = cursor;
            cursor = cursor.getNextElement();
            return result;
        }
    }
}
