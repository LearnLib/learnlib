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
package de.learnlib.algorithms.ttt.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.graphs.Graph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Word;

import com.google.common.collect.Iterators;

import de.learnlib.api.MembershipOracle;

/**
 * The discrimination tree data structure.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class DiscriminationTree<I,D> {

	private final DTNode<I,D> root;
	
	private final MembershipOracle<I, D> oracle;
	
	public DiscriminationTree(MembershipOracle<I, D> oracle) {
		this.root = new DTNode<>();
		this.oracle = oracle;
	}
	
	/**
	 * Retrieves the root node of this tree.
	 * @return the root node of this tree.
	 */
	public DTNode<I,D> getRoot() {
		return root;
	}
	
	/**
	 * Sifts an access sequence provided by an object into the tree, starting
	 * at the root. This operation performs a "hard" sift, i.e.,
	 * it will not stop at temporary nodes.
	 * 
	 * @param asp the object providing the access sequence
	 * @return the leaf resulting from the sift operation
	 */
	public DTNode<I,D> sift(AccessSequenceProvider<I> asp) {
		return sift(asp, true);
	}
	
	/**
	 * Sifts an access sequence provided by an object into the tree,
	 * starting at the root. This can either be a "soft" sift, which stops
	 * either at the leaf <b>or</b> at the first temporary node, or a "hard" sift,
	 * stopping only at a leaf.
	 * 
	 * @param asp the object providing the access sequence
	 * @param hard
	 * @return
	 */
	public DTNode<I,D> sift(AccessSequenceProvider<I> asp, boolean hard) {
		return sift(asp.getAccessSequence(), hard);
	}
	
	public DTNode<I,D> sift(Word<I> word) {
		return sift(word, true);
	}
	
	public DTNode<I,D> sift(Word<I> word, boolean hard) {
		return sift(root, word, hard);
	}
	
	public DTNode<I,D> sift(DTNode<I,D> start, AccessSequenceProvider<I> asp, boolean hard) {
		return sift(start, asp.getAccessSequence(), hard);
	}
	
	public DTNode<I,D> sift(DTNode<I,D> start, Word<I> word, boolean hard) {
		DTNode<I,D> curr = start;
		
		while(!curr.isLeaf() && (hard || !curr.isTemp())) {
			D outcome = mqOut(word, curr.getDiscriminator());
			curr = curr.child(outcome);
		}
		
		return curr;
	}
	
	
	public DTNode<I,D> leastCommonAncestor(DTNode<I,D> node1, DTNode<I,D> node2) {
		int ddiff = node1.getDepth() - node2.getDepth();
		
		DTNode<I,D> curr1, curr2;
		if(ddiff < 0) {
			curr1 = node2;
			curr2 = node1;
			ddiff *= -1;
		}
		else {
			curr1 = node1;
			curr2 = node2;
		}
		
		for(int i = 0; i < ddiff; i++) {
			curr1 = curr1.getParent();
		}
		
		while(curr1 != curr2) {
			curr1 = curr1.getParent();
			curr2 = curr2.getParent();
		}
		
		return curr1;
	}
	
	
	private D mqOut(Word<I> prefix, Word<I> suffix) {
		return oracle.answerQuery(prefix, suffix);
	}
	
	
	public class GraphView implements Graph<DTNode<I,D>, DTNode<I,D>> {

		@Override
		public Collection<? extends DTNode<I,D>> getNodes() {
			List<DTNode<I,D>> nodes = new ArrayList<>();
			
			Iterators.addAll(nodes, root.subtreeNodesIterator());
			
			return nodes;
		}

		@Override
		public Collection<? extends DTNode<I,D>> getOutgoingEdges(DTNode<I,D> node) {
			if(node.isLeaf()) {
				return Collections.emptyList();
			}
			return node.getChildren();
		}

		@Override
		public DTNode<I,D> getTarget(DTNode<I,D> edge) {
			return edge;
		}

		@Override
		public GraphDOTHelper<DTNode<I,D>, DTNode<I,D>> getGraphDOTHelper() {
			return new DefaultDOTHelper<DTNode<I,D>,DTNode<I,D>>() {

				@Override
				public boolean getNodeProperties(DTNode<I,D> node,
						Map<String, String> properties) {
					if(node.isLeaf()) {
						properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
						properties.put(NodeAttrs.LABEL, String.valueOf(node.state));
					}
					else {
						properties.put(NodeAttrs.LABEL, node.getDiscriminator().toString());
						if(!node.isTemp()) {
							properties.put(NodeAttrs.SHAPE, NodeShapes.OVAL);
						}
						else if(node.isBlockRoot()) {
							properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLEOCTAGON);
						}
						else {
							properties.put(NodeAttrs.SHAPE, NodeShapes.OCTAGON);
						}
					}
					
					return true;
				}

				@Override
				public boolean getEdgeProperties(DTNode<I,D> src, DTNode<I,D> edge,
						DTNode<I,D> tgt, Map<String, String> properties) {
					properties.put(EdgeAttrs.LABEL, String.valueOf(edge.getParentEdgeLabel()));
					
					return true;
				}
				
			};
		}
		
	}
	
	public GraphView graphView() {
		return new GraphView();
	}

}
