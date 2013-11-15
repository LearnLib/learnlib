/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.filters.reuse.api;

import de.learnlib.filters.reuse.ReuseTreeImpl;

/**
 * A {@link ReuseEdge} connects two vertices in the {@link ReuseTreeImpl} and is
 * labeled with input and output behaviour.
 *
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class ReuseEdge<S, I, O> {
	private ReuseNode<S, I, O> source;
	private ReuseNode<S, I, O> target;
	private I input;
	private O output;

	public ReuseEdge(ReuseNode<S, I, O> source, ReuseNode<S, I, O> target, I input, O output) {
		if (source == null) {
			throw new IllegalArgumentException("Source not allowed to be null.");
		}
		if (target == null) {
			throw new IllegalArgumentException("Target not allowed to be null.");
		}
		if (input == null) {
			throw new IllegalArgumentException("Input not allowed to be null.");
		}
		if (output == null) {
			throw new IllegalArgumentException("Output not allowed to be null.");
		}
		this.source = source;
		this.target = target;
		this.input = input;
		this.output = output;
	}

	public ReuseNode<S, I, O> getSource() {
		return source;
	}

	public ReuseNode<S, I, O> getTarget() {
		return target;
	}

	public I getInput() {
		return input;
	}

	public O getOutput() {
		return output;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(source.toString());
		sb.append(" -> ");
		sb.append(target.toString());
		sb.append(" i/o ");
		sb.append(input);
		sb.append("/");
		sb.append(output);
		return sb.toString();
	}
}
