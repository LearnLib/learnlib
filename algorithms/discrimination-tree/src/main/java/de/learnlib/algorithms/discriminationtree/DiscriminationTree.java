package de.learnlib.algorithms.discriminationtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.automatalib.graphs.abstractimpl.AbstractGraph;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.words.Word;

import com.google.common.collect.Iterables;

import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.MQUtil;

public class DiscriminationTree<I, O, D> extends AbstractGraph<DTNode<I,O,D>, Map.Entry<O, DTNode<I,O,D>>>
		implements DOTPlottableGraph<DTNode<I,O,D>, Map.Entry<O,DTNode<I,O,D>>> {
	
	private final MembershipOracle<I, O> oracle;
	private final DTNode<I,O,D> root;

	public DiscriminationTree(D rootData, MembershipOracle<I, O> oracle) {
		this.root = new DTNode<>(rootData);
		this.oracle = oracle;
	}
	
	
	public DTNode<I,O,D> sift(Word<I> prefix) {
		return sift(root, prefix);
	}
	
	public DTNode<I,O,D> sift(DTNode<I,O,D> start, Word<I> prefix) {
		DTNode<I,O,D> curr = start;
		
		while(!curr.isLeaf()) {
			O out = MQUtil.output(oracle, prefix, curr.getDiscriminator());
			curr = curr.child(out);
		}
		
		return curr;
	}
	
	
	/*
	 * AutomataLib Graph API
	 */
	
	
	@Override
	public Collection<DTNode<I, O, D>> getNodes() {
		List<DTNode<I,O,D>> nodes = new ArrayList<>(); 
		Iterables.addAll(nodes, GraphTraversal.breadthFirstOrder(this, Collections.singleton(root)));
		return nodes;
	}

	@Override
	public Collection<Map.Entry<O,DTNode<I,O,D>>> getOutgoingEdges(
			DTNode<I, O, D> node) {
		if(node.isLeaf())
			return Collections.emptySet();
		return node.getChildEntries();
	}

	@Override
	public DTNode<I, O, D> getTarget(Map.Entry<O, DTNode<I, O, D>> edge) {
		return edge.getValue();
	}
	
	public DTNode<I,O,D> getRoot() {
		return root;
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
					properties.put(SHAPE, "box");
					properties.put(LABEL, String.valueOf(node.getData()));
				}
				else {
					Word<I> d = node.getDiscriminator();
					properties.put(LABEL, d.toString());
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
				properties.put(LABEL, String.valueOf(edge.getKey()));
				return true;
			}
		};
	}



}
