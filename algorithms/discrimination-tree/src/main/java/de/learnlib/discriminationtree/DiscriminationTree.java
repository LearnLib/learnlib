/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.discriminationtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.automatalib.graphs.Graph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.words.Word;

import com.google.common.collect.Iterables;

import de.learnlib.api.MembershipOracle;

public class DiscriminationTree<I, O, D> {
	
	private final MembershipOracle<I, O> oracle;
	private final DTNode<I,O,D> root;
	
	public DiscriminationTree(DTNode<I,O,D> root, MembershipOracle<I, O> oracle) {
		this.root = root;
		this.oracle = oracle;
	}
	
	
	public DTNode<I,O,D> sift(Word<I> prefix) {
		return sift(root, prefix);
	}
	
	public DTNode<I,O,D> sift(DTNode<I,O,D> start, Word<I> prefix) {
		DTNode<I,O,D> curr = start;
		
		while(!curr.isLeaf()) {
			O out = oracle.answerQuery(prefix, curr.getDiscriminator());
			curr = curr.child(out);
		}
		
		return curr;
	}
	

	public DTNode<I,O,D> getRoot() {
		return root;
	}
	
	public DTNode<I,O,D> leastCommonAncestor(DTNode<I,O,D> node1, DTNode<I,O,D> node2) {
		int d1 = node1.depth;
		int d2 = node2.depth;
		
		int ddiff = d2 - d1;
		
		DTNode<I,O,D> curr1, curr2;
		if(ddiff >= 0) {
			curr1 = node1;
			curr2 = node2;
		}
		else {
			curr1 = node2;
			curr2 = node1;
			ddiff *= -1;
		}
		
		while(ddiff > 0) {
			curr2 = curr2.parent;
			ddiff--;
		}
		
		if(curr1 == curr2) {
			return curr1;
		}
		
		while(curr1 != curr2) {
			curr1 = curr1.parent;
			curr2 = curr2.parent;
		}
		
		return curr1;
	}
	
	public static class LCAInfo<I,O,D> {
		public final DTNode<I,O,D> leastCommonAncestor;
		public final O subtree1Label;
		public final O subtree2Label;
		
		public LCAInfo(DTNode<I,O,D> leastCommonAncestor, O subtree1Label, O subtree2Label) {
			this(leastCommonAncestor, subtree1Label, subtree2Label, false);
		}
		
		private LCAInfo(DTNode<I,O,D> leastCommonAncestor, O subtree1Label, O subtree2Label, boolean swap) {
			this.leastCommonAncestor = leastCommonAncestor;
			if(swap) {
				this.subtree1Label = subtree2Label;
				this.subtree2Label = subtree1Label;
			}
			else {
				this.subtree1Label = subtree1Label;
				this.subtree2Label = subtree2Label;
			}
		}
	}
	
	public LCAInfo<I,O,D> lcaInfo(DTNode<I,O,D> node1, DTNode<I,O,D> node2) {
		int d1 = node1.depth;
		int d2 = node2.depth;
		
		int ddiff = d2 - d1;
		
		boolean swap = false;
		
		DTNode<I,O,D> curr1, curr2;
		if(ddiff >= 0) {
			curr1 = node1;
			curr2 = node2;
		}
		else {
			curr1 = node2;
			curr2 = node1;
			ddiff *= -1;
			swap = true;
		}
		
		O out1 = null, out2 = null;
		while(ddiff > 0) {
			out2 = curr2.parentOutcome;
			curr2 = curr2.parent;
			ddiff--;
		}
		
		if(curr1 == curr2) {
			return new LCAInfo<>(curr1, out1, out2, swap);
		}
		
		while(curr1 != curr2) {
			out1 = curr1.parentOutcome;
			curr1 = curr1.parent;
			out2 = curr2.parentOutcome;
			curr2 = curr2.parent;
		}
		
		return new LCAInfo<>(curr1, out1, out2, swap);
	}
	
	/*
	 * AutomataLib Graph API
	 */
	
	public class GraphView implements Graph<DTNode<I,O,D>,Map.Entry<O,DTNode<I,O,D>>> {
		
		@Override
		public Collection<DTNode<I, O, D>> getNodes() {
			List<DTNode<I,O,D>> nodes = new ArrayList<>(); 
			Iterables.addAll(nodes, GraphTraversal.breadthFirstOrder(this, Collections.singleton(root)));
			return nodes;
		}
	
		@Override
		public Collection<Map.Entry<O,DTNode<I,O,D>>> getOutgoingEdges(
				DTNode<I, O, D> node) {
			if(node.isLeaf()) {
				return Collections.emptySet();
			}
			return node.getChildEntries();
		}
	
		@Override
		public DTNode<I, O, D> getTarget(Map.Entry<O, DTNode<I, O, D>> edge) {
			return edge.getValue();
		}

		@Override
		public GraphDOTHelper<DTNode<I, O, D>, Entry<O, DTNode<I, O, D>>> getGraphDOTHelper() {
			return new DefaultDOTHelper<DTNode<I,O,D>,Map.Entry<O,DTNode<I,O,D>>>() {

				/* (non-Javadoc)
				 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
				 */
				@Override
				public boolean getNodeProperties(DTNode<I, O, D> node,
						Map<String, String> properties) {
					if(!super.getNodeProperties(node, properties))
						return false;
					if(node.isLeaf()) {
						properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
						properties.put(NodeAttrs.LABEL, String.valueOf(node.getData()));
					}
					else {
						Word<I> d = node.getDiscriminator();
						properties.put(NodeAttrs.SHAPE, NodeShapes.OVAL);
						properties.put(NodeAttrs.LABEL, d.toString());
					}
					return true;
				}

				/* (non-Javadoc)
				 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getEdgeProperties(java.lang.Object, java.lang.Object, java.lang.Object, java.util.Map)
				 */
				@Override
				public boolean getEdgeProperties(DTNode<I, O, D> src,
						Entry<O, DTNode<I, O, D>> edge, DTNode<I, O, D> tgt,
						Map<String, String> properties) {
					if(!super.getEdgeProperties(src, edge, tgt, properties))
						return false;
					properties.put(EdgeAttrs.LABEL, String.valueOf(edge.getKey()));
					return true;
				}
			};
		}
	}
	
	public GraphView graphView() {
		return new GraphView();
	}
}
