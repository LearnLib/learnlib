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
package de.learnlib.datastructure.list;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.automatalib.common.smartcollection.IntrusiveLinkedList;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A list that stores elements which directly expose information about their predecessor and successor elements. Similar
 * to {@link IntrusiveLinkedList}, this list uses doubly-linked entries. However, its elements can
 * {@link IntrusiveListEntry#removeFromList() remove} themselves from the list they are currently part of. In order to
 * ease the management of the start (head) of the list, the list itself is the first element so that removal only
 * requires predecessor/successor pointer management.
 *
 * @param <T>
 *         element type
 */
public class IntrusiveList<T extends IntrusiveListEntry<T>> extends AbstractIntrusiveListEntryImpl<T>
        implements Iterable<T> {

    /**
     * Adds an element to the front of the list. Note that this method also removes the element from the list it may
     * currently be contained in.
     *
     * @param elem
     *         the element to add
     */
    public void add(T elem) {
        elem.removeFromList();

        final IntrusiveListEntry<T> next = getNext();
        elem.setNext(next);
        if (next != null) {
            next.setPrev(elem);
        }

        elem.setPrev(this);
        setNext(elem);
    }

    /**
     * Concatenates (prepends) the given list with {@code this} list. The given list will be empty after this method
     * terminates.
     *
     * @param list
     *         the list to add to {@code this} list
     */
    public void concat(IntrusiveList<T> list) {
        final IntrusiveListEntry<T> next = getNext();
        final IntrusiveListEntry<T> lNext = list.getNext();

        if (lNext != null) {
            IntrusiveListEntry<T> last = lNext;
            IntrusiveListEntry<T> tmp;
            while ((tmp = last.getNext()) != null) {
                last = tmp;
            }
            if (next != null) {
                next.setPrev(last);
            }
            last.setNext(next);
            lNext.setPrev(this);
            setNext(lNext);
        }
        list.setNext(null);
    }

    /**
     * Retrieves any element from the list. If the list is empty, {@code null} is returned.
     *
     * @return any block from the list, or {@code null} if the list is empty.
     */
    public @Nullable T choose() {
        final IntrusiveListEntry<T> next = getNext();

        return next == null ? null : next.getElement();
    }

    /**
     * Retrieves and removes the first element from this list.
     *
     * @return the head of the list, or {@code null} if the list is empty
     */
    public @Nullable T poll() {
        final IntrusiveListEntry<T> result = getNext();
        if (result == null) {
            return null;
        }
        final IntrusiveListEntry<T> next = result.getNext();
        setNext(next);
        if (next != null) {
            next.setPrev(this);
        }

        result.setPrev(null);
        result.setNext(null);

        return result.getElement();
    }

    /**
     * Returns this size of this list, i.e., the number of elements excluding the self-referential head reference.
     *
     * @return the size of the list
     */
    public int size() {
        IntrusiveListEntry<T> curr = getNext();
        int i = 0;
        while (curr != null) {
            i++;
            curr = curr.getNext();
        }

        return i;
    }

    /**
     * Returns whether this list is empty.
     *
     * @return {@code true} if the list is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return getNext() == null;
    }

    @Override
    public Iterator<T> iterator() {
        return new ListIterator(getNext());
    }

    @Override
    public T getElement() {
        throw new UnsupportedOperationException("this method should never be called on the head of the list");
    }

    private class ListIterator implements Iterator<T> {

        private @Nullable IntrusiveListEntry<T> cursor;

        ListIterator(@Nullable IntrusiveListEntry<T> start) {
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

            final IntrusiveListEntry<T> result = cursor;
            cursor = cursor.getNext();
            return result.getElement();
        }
    }
}
