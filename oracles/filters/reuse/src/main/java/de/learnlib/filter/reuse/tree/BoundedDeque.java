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
package de.learnlib.filter.reuse.tree;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * A generic deque-derived container which transparently acts <i>either</i> as a stack <i>or</i> a queue, and optionally
 * a capacity restriction with a configurable policy which element is evicted (or reject) if the maximum capacity is
 * reached.
 * <p>
 * <b>Note:</b> Like {@link ArrayDeque}, this deque implementation is not thread-safe. Concurrent access by multiple
 * threads requires explicit synchronization.
 *
 * @param <E>
 *         element type
 *
 * @author Malte Isberner
 */
public class BoundedDeque<E> extends AbstractCollection<E> implements Serializable {

    private static final long serialVersionUID = 1L;
    private final EvictPolicy evictPolicy;
    private final AccessPolicy accessPolicy;
    private final int capacity;
    private final Deque<E> deque;

    /**
     * Constructor. Creates an unbounded deque with the given access policy.
     *
     * @param accessPolicy
     *         whether this deque acts as a stack or a queue
     */
    public BoundedDeque(AccessPolicy accessPolicy) {
        this(-1, accessPolicy, EvictPolicy.EVICT_OLDEST); // policy does not matter
    }

    /**
     * Constructor. Creates a possibly capacity-restricted deque with the given access policy.
     *
     * @param capacity
     *         the maximum capacity of this deque. A value less than or equal to 0 means unbounded
     * @param accessPolicy
     *         whether this deque acts as a stack or a queue
     * @param evictPolicy
     *         which elements to remove if the maximum capacity is reached. If the capacity is unbounded, this parameter
     *         has no effect
     */
    public BoundedDeque(int capacity, AccessPolicy accessPolicy, EvictPolicy evictPolicy) {
        if (capacity <= 0) {
            this.deque = new ArrayDeque<>(); // unbounded, default capacity
        } else {
            this.deque = new ArrayDeque<>(capacity);
        }
        this.capacity = capacity;
        this.accessPolicy = accessPolicy;
        this.evictPolicy = evictPolicy;
    }

    /**
     * Inserts an element into the deque, and returns the one that had to be evicted in case of a capacity violation.
     *
     * @param element
     *         the element to insert
     *
     * @return the evicted element, {@code null} if the maximum capacity has not been reached
     */
    public E insert(E element) {
        E evicted = null;
        if (size() >= capacity) {
            if (evictPolicy == EvictPolicy.REJECT_NEW) {
                // reject the new element
                return element;
            }
            // Evict first, so we do not need to resize
            evicted = evict();
        }
        deque.offerLast(element);
        return evicted;
    }

    /**
     * Evicts an element, depending on the configured {@link EvictPolicy}.
     *
     * @return the evicted element
     */
    private E evict() {
        switch (evictPolicy) {
            case EVICT_OLDEST:
                return deque.pollFirst();
            case EVICT_NEWEST:
                return deque.pollLast();
            default:
                throw new IllegalStateException("Illegal evict policy: " + evictPolicy);
        }

    }

    /**
     * Retrieves and remove the top-most element, i.e., the element that is either the top of the stack or the head of
     * the queue, depending on the configured {@link AccessPolicy}.
     *
     * @return the top-most element of the container
     */
    public E retrieve() {
        switch (accessPolicy) {
            case LIFO:
                return deque.pollLast();
            case FIFO:
                return deque.pollFirst();
            default:
                throw new IllegalStateException("Illegal evict policy: " + evictPolicy);
        }
    }

    /**
     * Retrieves, but does not remove the top-most element, i.e., the element that is either the top of the stack or the
     * head of the queue, depending on the configured {@link AccessPolicy}.
     *
     * @return the top-most element of the container
     */
    public E peek() {
        switch (accessPolicy) {
            case LIFO:
                return deque.peekLast();
            case FIFO:
                return deque.peekFirst();
            default:
                throw new IllegalStateException("Illegal evict policy: " + evictPolicy);
        }
    }

    /**
     * Retrieves whether capacity restriction is in effect.
     *
     * @return {@code true} if the capacity is restricted, {@code false} otherwise
     */
    public boolean isBounded() {
        return (capacity > 0);
    }

    @Override
    public Iterator<E> iterator() {
        return deque.iterator();
    }

    @Override
    public int size() {
        return deque.size();
    }

    @Override
    public boolean isEmpty() {
        return deque.isEmpty();
    }

    @Override
    public void clear() {
        deque.clear();
    }

    /**
     * The policy which determines in which order elements are accessed.
     *
     * @author Malte Isberner
     */
    public enum AccessPolicy {
        /**
         * Last-in first-out. This deque will then act as a <i>stack</i>.
         */
        LIFO,
        /**
         * First-in first-out. This deque will then act as a <i>queue</i>.
         */
        FIFO
    }

    /**
     * The policy which determines in which order elements are removed if the maximum capacity is reached.
     *
     * @author Malte Isberner
     */
    public enum EvictPolicy {
        /**
         * Evict the oldest element, i.e., the one at the head of the queue/bottom of the stack.
         */
        EVICT_OLDEST,
        /**
         * Reject the element that is about to be inserted.
         */
        REJECT_NEW,
        /**
         * Evict the newest element, that is already *in* the queue (i.e., in any case inserts the new element).
         */
        EVICT_NEWEST
    }

}
