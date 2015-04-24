package de.learnlib.passive.rpni;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AbstractPTA<SP,TP,S extends AbstractPTAState<SP,TP,S>> {

	protected final int alphabetSize;
	protected final S root;
	
	public AbstractPTA(int alphabetSize, S root) {
		this.alphabetSize = alphabetSize;
		this.root = root;
	}
	
	
	public S getRoot() {
		return root;
	}
	
	public List<S> bfsStates() {
		List<S> stateList = new ArrayList<>();
		Set<S> visited = new HashSet<>();
	
		int ptr = 0;
		stateList.add(root);
		visited.add(root);
		int numStates = 1;
		
		while (ptr < numStates) {
			S curr = stateList.get(ptr++);
			for (int i = 0; i < alphabetSize; i++) {
				S succ = curr.getSuccessor(i);
				if (succ != null && visited.add(succ)) {
					stateList.add(succ);
					numStates++;
				}
			}
		}
		
		return stateList;
	}
	
	public Iterator<S> bfsIterator() {
		Set<S> visited = new HashSet<>();
		final Deque<S> bfsQueue = new ArrayDeque<>();
		bfsQueue.add(root);
		visited.add(root);
		
		return new AbstractIterator<S>() {
			@Override
			protected S computeNext() {
				S next = bfsQueue.poll();
				if (next == null) {
					return endOfData();
				}
				for (int i = 0; i < alphabetSize; i++) {
					S child = next.getSuccessor(i);
					if (child != null && visited.add(child)) {
						bfsQueue.offer(child);
					}
				}
				return next;
			}
		};
	}
	
	public S getState(int[] word) {
		S curr = root;
		int len = word.length;
		for (int i = 0; i < len && curr != null; i++) {
			curr = curr.getSuccessor(word[i]);
		}
		return curr;
	}
	
	public S getOrCreateState(int[] word) {
		S curr = root;
		for (int sym : word) {
			curr = curr.getOrCreateSuccessor(sym, alphabetSize);
		}
		
		return curr;
	}
	
	public void addSample(int[] sample, SP lastProperty) {
		S target = getOrCreateState(sample);
		if (!target.tryMergeStateProperty(lastProperty)) {
			throw new IllegalStateException();
		}
	}
	
	public void addSampleOutput(int[] sample, D output) {
		PTAState<D> target = getState(sample);
		if (target != null) {
			if (!target.setOutput(output)) {
				throw new IllegalStateException();
			}
		}
	}
	
	public boolean testSample(int[] sample, D expectedOutput) {
		PTAState<D> target = getState(sample);
		return target.isOutputCompatible(expectedOutput);
	}
	
	public boolean testSampleStrict(int[] sample, D expectedOutput) {
		PTAState<D> target = getState(sample);
		return target != null && target.isOutputCompatible(expectedOutput);
	}
	
	public int[] testSamples(Collection<int[]> samples, D expectedOutput) {
		return samples.stream()
				.filter(s -> !testSample(s, expectedOutput))
				.findAny()
				.orElse(null);
	}
	
	public int[] testSamplesStrict(Collection<int[]> samples, D expectedOutput) {
		return samples.stream()
				.filter(s -> !testSampleStrict(s, expectedOutput))
				.findAny()
				.orElse(null);
	}
	
	public void addSamplesOutput(Collection<int[]> samples, D output) {
		samples.stream().forEach(s -> addSampleOutput(s, output));
	}
	
	public void addSamples(Collection<int[]> samples, D output) {
		samples.stream().forEach(s -> addSample(s, output));
	}

		
	static final class FoldRecord<D> {
		public PTAState<D> q;
		public final PTAState<D> r;
		public int i = -1;
		
		public FoldRecord(PTAState<D> q, PTAState<D> r) {
			this.q = q;
			this.r = r;
		}
	}
	
	public <S2,I,SP2,TP2> void toAutomaton(MutableDeterministic<S2, I, ?, ? super SP2, ? super TP2> automaton,
			Alphabet<I> alphabet,
			Function<? super SP,? extends SP2> spExtractor,
			Function<? super TP,? extends TP2> tpExtractor) {
		
		Map<S,S2> resultStates = new HashMap<>();
		
		Queue<Pair<S,S2>> queue = new ArrayDeque<>();
		
		SP2 initProp = spExtractor.apply(root.getStateProperty());
		S2 resultInit = automaton.addInitialState(initProp);
		queue.add(new Pair<>(root, resultInit));
		
		Pair<S,S2> curr;
		while ((curr = queue.poll()) != null) {
			S ptaState = curr.getFirst();
			S2 resultState = curr.getSecond();
			
			for (int i = 0; i < alphabetSize; i++) {
				S ptaSucc = ptaState.getSuccessor(i);
				if (ptaSucc != null) {
					S2 resultSucc = resultStates.get(ptaSucc);
					if (resultSucc == null) {
						SP2 prop = spExtractor.apply(ptaSucc.getStateProperty());
						resultSucc = automaton.addState(prop);
						resultStates.put(ptaSucc, resultSucc);
						queue.offer(new Pair<>(ptaSucc, resultSucc));
					}
					I sym = alphabet.getSymbol(i);
					TP2 transProp = tpExtractor.apply(ptaState.getTransProperty(i));
					automaton.setTransition(resultState, sym, resultSucc, transProp);
				}
			}
		}
	}
	
	
	public RedBlueMerge<D> tryMerge(PTAState<D> red, PTAState<D> blue) {
		RedBlueMerge<D> mod = new RedBlueMerge<>(this, red, blue);
		if (!mod.merge()) {
			return null;
		}
		
		return mod;
	}
	
	public static final class Edge<S> {
		public final S state;
		public final int input;
		
		public Edge(S state, int input) {
			this.state = state;
			this.input = input;
		}
	}
	
	public <I> Graph<S,Edge<S>> graphView(Alphabet<I> alphabet) {
		return new Graph<S,Edge<S>>() {

			@Override
			public Collection<? extends Edge<S>> getOutgoingEdges(S node) {
				return IntStream.range(0, alphabetSize).mapToObj(i -> {
					S succ = node.getSuccessor(i);
					if (succ == null) {
						return null;
					}
					return new Edge<>(succ, i);
				}).filter(e -> e != null).collect(Collectors.toList());
			}

			@Override
			public S getTarget(Edge<S> edge) {
				return edge.state;
			}

			@Override
			public Iterator<S> iterator() {
				return bfsIterator();
			}

			@Override
			public Collection<? extends S> getNodes() {
				return bfsStates();
			}
			
			
			@Override
			public GraphDOTHelper<S, Edge<S>> getGraphDOTHelper() {
				return new EmptyDOTHelper<S, Edge<S>>() {
					@Override
					public boolean getEdgeProperties(S src,
							Edge<S> edge, S tgt,
							Map<String, String> properties) {
						if (!super.getEdgeProperties(src, edge, tgt, properties)) {
							return false;
						}
						properties.put(EdgeAttrs.LABEL, String.valueOf(alphabet.getSymbol(edge.input)));
						return true;
					}
					
				};
			}
		};
	}
}
