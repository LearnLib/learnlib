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
package de.learnlib.filters.reuse.tree;

import java.util.Map;

import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;

/**
 * {@link GraphDOTHelper} implementation for the {@link ReuseTree} that renders
 * nodes as white circles (if no system state is available on the node) or black 
 * diamonds (otherwise). 
 * Edges are labeled with input / output behavior.
 * 
 * @author Oliver Bauer 
 *
 * @param <S> system state class
 * @param <I> input symbol class
 * @param <O> output symbol class
 */
class ReuseTreeDotHelper<S, I, O> extends
DefaultDOTHelper<ReuseNode<S, I, O>, ReuseEdge<S, I, O>> {
	

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getNodeProperties(ReuseNode<S, I, O> node,
			Map<String, String> properties) {
		super.getNodeProperties(node, properties);
		if (node.hasSystemStates()) {
			properties.put(NodeAttrs.COLOR, "black");
			properties.put(NodeAttrs.STYLE, "filled");
			properties.put(NodeAttrs.SHAPE, "diamond");
			properties.put("fontcolor", "white");
		}
//		properties.put(NodeAttrs.LABEL, String.valueOf(node.getId()));
		properties.put(NodeAttrs.LABEL, "");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getEdgeProperties(java.lang.Object, java.lang.Object, java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getEdgeProperties(ReuseNode<S, I, O> src,
			ReuseEdge<S, I, O> edge, ReuseNode<S, I, O> tgt,
			Map<String, String> properties) {
		super.getEdgeProperties(src, edge, tgt, properties);
		String label = String.valueOf(edge.getInput()) + " / ";
		O output = edge.getOutput();
		if (output != null)
			label += String.valueOf(output);
		properties.put(EdgeAttrs.LABEL, label);
		return true;
	}
}
