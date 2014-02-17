/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.filters.reuse.tree;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * A generic deque-derived container which transparently acts <i>either</i> as a stack <i>or</i>
 * a queue, and optionally a capacity restriction with a configurable policy which element is
 * evicted (or reject) if the maximum capacity is reached.
 * <p>
 * <b>Note:</b> Like {@link ArrayDeque}, this deque implementation is not thread-safe. Concurrent
 * access by multiple threads requires explicit synchronization.
 * 
 * @author Malte Isberner
 *
 * @param <E> element type
 */
public class BoundedDeque<E> extends AbstractCollection<E> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The policy which determines in which order elements are accessed.
	 *  
	 * @author Malte Isberner
	 *
	 */
	public static enum AccessPolicy {
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
	 * The policy which determines in which order elements are removed if
	 * the maximum capacity is reached.
	 * 
	 * @author Malte Isberner
	 *
	 */
	public static enum EvictPolicy {
		/**
		 * Evict the oldest element, i.e., the one at the head of the queue/bottom
		 * of the stack.
		 */
		EVICT_OLDEST,
		/**
		 * Reject the element that is about to be inserted.
		 */
		REJECT_NEW,
		/**
		 * Evict the newest element, that is already *in* the queue (i.e., in any case
		 * inserts the new element).
		 * 
		 */
		EVICT_NEWEST
	}
	
	private final EvictPolicy evictPolicy;
	private final AccessPolicy accessPolicy;
	
	private final int capacity;
	
	private final Deque<E> deque;

	/**
	 * Constructor. Creates an unbounded deque with the given access policy.
	 * 
	 * @param accessPolicy whether this deque acts as a stack or a queue
	 */
	public BoundedDeque(AccessPolicy accessPolicy) {
		this(-1, accessPolicy, EvictPolicy.EVICT_OLDEST); // policy does not matter
	}
	
	/**
	 * Constructor. Creates a possibly capacity-restricted deque with the
	 * given access policy.
	 * 
	 * @param capacity the maximum capacity of this deque. A value less than or equal to 0
	 * means unbounded 
	 * @param accessPolicy whether this deque acts as a stack or a queue
	 * @param evictPolicy which elements to remove if the maximum capacity is reached. If the
	 * capacity is unbounded, this parameter has no effect 
	 */
	public BoundedDeque(int capacity, AccessPolicy accessPolicy, EvictPolicy evictPolicy) {
		if(capacity <= 0) {
			this.deque = new ArrayDeque<>(); // unbounded, default capacity
		}
		else {
			this.deque = new ArrayDeque<>(capacity);
		}
		this.capacity = capacity;
		this.accessPolicy = accessPolicy;
		this.evictPolicy = evictPolicy;
	}
	
	/**
	 * Inserts an element into the deque, and returns the one that had to be
	 * evicted in case of a capacity violation.
	 * 
	 * @param element the element to insert
	 * @return the evicted element, {@code null} if the maximum capacity has not been reached
	 */
	public E insert(E element) {
		E evicted = null;
		if(size() >= capacity) {
			if(evictPolicy == EvictPolicy.REJECT_NEW) {
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
	 * Retrieves and remove the top-most element, i.e., the element that is either
	 * the top of the stack or the head of the queue, depending on the configured
	 * {@link AccessPolicy}.
	 * 
	 * @return the top-most element of the container
	 */
	public E retrieve() {
		switch(accessPolicy) {
		case LIFO:
			return deque.pollLast();
		case FIFO:
			return deque.pollFirst();
		}
		throw new IllegalStateException("Illegal access policy: " + accessPolicy);
	}
	
	/**
	 * Retrieves, but does not remove the top-most element, i.e., the element that is
	 * either the top of the stack or the head of the queue, depending on the configured
	 * {@link AccessPolicy}.
	 * 
	 * @return the top-most element of the container
	 */
	public E peek() {
		switch(accessPolicy) {
		case LIFO:
			return deque.peekLast();
		case FIFO:
			return deque.peekFirst();
		}
		throw new IllegalStateException("Illegal access policy: " + accessPolicy);
	}
	
	/**
	 * Retrieves whether capacity restriction is in effect.
	 * @return {@code true} if the capacity is restricted, {@code false} otherwise
	 */
	public boolean isBounded() {
		return (capacity > 0);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return deque.isEmpty();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return deque.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#clear()
	 */
	@Override
	public void clear() {
		deque.clear();
	}
	
	/**
	 * Evicts an element, depending on the configured {@link EvictPolicy}.
	 * @return the evicted element
	 */
	private E evict() {
		switch(evictPolicy) {
		case EVICT_OLDEST:
			return deque.pollFirst();
		case EVICT_NEWEST:
			return deque.pollLast();
		default:
			throw new IllegalStateException("Illegal evict policy: " + evictPolicy);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return deque.iterator();
	}

}
