package de.learnlib.algorithms.discriminationtree.hypothesis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.abstractimpl.AbstractAutomaton;
import net.automatalib.automata.abstractimpl.AbstractDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.abstractimpl.AbstractGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.api.AccessSequenceTransformer;

/**
 * Basic hypothesis data structure for Discrimination Tree learning algorithms.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol type
 * @param <O> SUL output type
 * @param <SP> state property type
 * @param <TP> transition property type
 * @param <T> exposed transition 
 */
public class DTLearnerHypothesis<I, O, SP, TP> extends
		AbstractGraph<HState<I, O, SP, TP>, HTransition<I, O, SP, TP>>
		implements
		DOTPlottableGraph<HState<I, O, SP, TP>, HTransition<I, O, SP, TP>>,
		UniversalDeterministicAutomaton<HState<I, O, SP, TP>, I, HTransition<I,O,SP,TP>, SP, TP>,
		NodeIDs<HState<I, O, SP, TP>>, StateIDs<HState<I, O, SP, TP>>,
		AccessSequenceTransformer<I> {

	private final Alphabet<I> alphabet;
	private final HState<I, O, SP, TP> root;
	private final List<HState<I, O, SP, TP>> nodes = new ArrayList<>();

	public DTLearnerHypothesis(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
		this.root = new HState<>(alphabet.size());
		this.nodes.add(root);
	}

	public HState<I, O, SP, TP> createState(
			HTransition<I, O, SP, TP> treeIncoming) {
		HState<I, O, SP, TP> state = new HState<>(alphabet.size(),
				nodes.size(), treeIncoming);
		nodes.add(state);
		treeIncoming.makeTree(state);
		return state;
	}

	@Override
	public HTransition<I, O, SP, TP> getTransition(
			HState<I, O, SP, TP> state, I symbol) {
		int symIdx = alphabet.getSymbolIndex(symbol);
		return state.getTransition(symIdx);
	}

	@Override
	public Collection<HState<I, O, SP, TP>> getNodes() {
		return Collections.unmodifiableCollection(nodes);
	}

	@Override
	public NodeIDs<HState<I, O, SP, TP>> nodeIDs() {
		return this;
	}

	@Override
	public Collection<HTransition<I, O, SP, TP>> getOutgoingEdges(
			HState<I, O, SP, TP> node) {
		return node.getOutgoingTransitions();
	}

	@Override
	public HState<I, O, SP, TP> getTarget(HTransition<I, O, SP, TP> edge) {
		return edge.currentTarget();
	}

	@Override
	public Collection<HTransition<I,O,SP,TP>> getTransitions(HState<I, O, SP, TP> state, I input) {
		return AbstractDeterministicAutomaton.getTransitions(this, state,
				input);
	}

	@Override
	public DeterministicTransitionSystem<? extends Set<HState<I, O, SP, TP>>, I, ? extends Collection<HTransition<I,O,SP,TP>>> powersetView() {
		return AbstractDeterministicAutomaton.powersetView(this);
	}

	@Override
	public <V> MutableMapping<HState<I, O, SP, TP>, V> createDynamicStateMapping() {
		return AbstractAutomaton.createDynamicStateMapping(this);
	}

	@Override
	public <V> MutableMapping<HState<I, O, SP, TP>, V> createStaticStateMapping() {
		return AbstractDeterministicAutomaton
				.createStaticStateMapping(this);
	}

	@Override
	public Set<HState<I, O, SP, TP>> getInitialStates() {
		return AbstractDeterministicAutomaton.getInitialStates(this);
	}

	@Override
	public Set<HState<I, O, SP, TP>> getStates(Iterable<I> input) {
		return AbstractDeterministicAutomaton.getStates(this, input);
	}

	@Override
	public Set<HState<I, O, SP, TP>> getSuccessors(
			HState<I, O, SP, TP> state, I input) {
		return AbstractDeterministicAutomaton.getSuccessors(this, state,
				input);
	}

	@Override
	public Set<HState<I, O, SP, TP>> getSuccessors(
			HState<I, O, SP, TP> state, Iterable<I> input) {
		return AbstractDeterministicAutomaton.getSuccessors(this, state,
				input);
	}

	@Override
	public Set<HState<I, O, SP, TP>> getSuccessors(
			Collection<HState<I, O, SP, TP>> states, Iterable<I> input) {
		return AbstractDeterministicAutomaton.getSuccessors(this, states,
				input);
	}

	@Override
	public Collection<HState<I, O, SP, TP>> getStates() {
		return Collections.unmodifiableCollection(nodes);
	}

	@Override
	public StateIDs<HState<I, O, SP, TP>> stateIDs() {
		return this;
	}

	@Override
	public HState<I, O, SP, TP> getInitialState() {
		return root;
	}

	@Override
	public HState<I, O, SP, TP> getState(Iterable<I> input) {
		return AbstractDeterministicAutomaton.getState(this, input);
	}

	@Override
	public HState<I, O, SP, TP> getSuccessor(HState<I, O, SP, TP> state,
			I input) {
		return AbstractDeterministicAutomaton.getSuccessor(this, state,
				input);
	}

	@Override
	public HState<I, O, SP, TP> getSuccessor(HState<I, O, SP, TP> state,
			Iterable<I> input) {
		return AbstractDeterministicAutomaton.getSuccessor(this, state,
				input);
	}


	@Override
	public SP getStateProperty(HState<I, O, SP, TP> state) {
		return state.getProperty();
	}

	@Override
	public HState<I, O, SP, TP> getState(int id) {
		return nodes.get(id);
	}

	@Override
	public int getStateId(HState<I, O, SP, TP> state) {
		return state.getId();
	}

	@Override
	public HState<I, O, SP, TP> getNode(int id) {
		return nodes.get(id);
	}

	@Override
	public int getNodeId(HState<I, O, SP, TP> node) {
		return node.getId();
	}

	@Override
	public GraphDOTHelper<HState<I, O, SP, TP>, HTransition<I, O, SP, TP>> getGraphDOTHelper() {
		return new DefaultDOTHelper<HState<I, O, SP, TP>, HTransition<I, O, SP, TP>>() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * net.automatalib.graphs.dot.DefaultDOTHelper#initialNodes()
			 */
			@Override
			protected Collection<? extends HState<I, O, SP, TP>> initialNodes() {
				return Collections.singleton(root);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * net.automatalib.graphs.dot.DefaultDOTHelper#getNodeProperties
			 * (java.lang.Object, java.util.Map)
			 */
			@Override
			public boolean getNodeProperties(HState<I, O, SP, TP> node,
					Map<String, String> properties) {
				if (!super.getNodeProperties(node, properties))
					return false;
				properties.put(LABEL, node.toString());
				return true;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * net.automatalib.graphs.dot.DefaultDOTHelper#getEdgeProperties
			 * (java.lang.Object, java.lang.Object, java.lang.Object,
			 * java.util.Map)
			 */
			@Override
			public boolean getEdgeProperties(HState<I, O, SP, TP> src,
					HTransition<I, O, SP, TP> edge,
					HState<I, O, SP, TP> tgt, Map<String, String> properties) {
				if (!super.getEdgeProperties(src, edge, tgt, properties))
					return false;
				properties.put(LABEL, String.valueOf(edge.getSymbol()));
				if (edge.isTree())
					properties.put("style", "bold");
				return true;
			}
		};
	}

	@Override
	public boolean isAccessSequence(Word<I> word) {
		HState<I, O, SP, TP> curr = root;
		for (I sym : word) {
			int symIdx = alphabet.getSymbolIndex(sym);
			HTransition<I, O, SP, TP> trans = curr.getTransition(symIdx);
			if (trans.isTree())
				return false;
			curr = trans.getTreeTarget();
		}
		return true;
	}

	@Override
	public Word<I> transformAccessSequence(Word<I> word) {
		HState<I, O, SP, TP> state = getState(word);
		return state.getAccessSequence();
	}

	@Override
	public HState<I, O, SP, TP> getSuccessor(HTransition<I, O, SP, TP> trans) {
		return trans.currentTarget();
	}

	@Override
	public TP getTransitionProperty(HTransition<I, O, SP, TP> trans) {
		return trans.getProperty();
	}

}