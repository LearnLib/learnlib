package de.learnlib.passive.rpni;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;

public class RPNI<I> implements PassiveDFALearner<I> {
	
	private final Alphabet<I> alphabet;
	private final int alphabetSize;
	private final List<int[]> positive = new ArrayList<>();
	private final List<int[]> negative = new ArrayList<>();
	
	private boolean parallel = true;
	private boolean deterministic = false;
	private boolean complete = false;

	public RPNI(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
		this.alphabetSize = alphabet.size();
	}

	@Override
	public void addSamples(
			Collection<? extends DefaultQuery<I, Boolean>> samples) {
		for (DefaultQuery<I, Boolean> sample : samples) {
			int[] encoded = sample.getInput().toIntArray(alphabet);
			Boolean out = sample.getOutput();
			if (out.booleanValue()) {
				positive.add(encoded);
			}
			else {
				negative.add(encoded);
			}
		}
	}

	@Override
	public DFA<?, I> computeModel() {
		PTA<Boolean> pta = new PTA<>(alphabetSize);
		pta.addSamples(positive, true);
		pta.addSamples(negative, false);

		assert pta.testSamplesStrict(positive, true) == null;
		assert pta.testSamples(negative, false) == null;
		
		Queue<BlueStateRef<Boolean>> blue = new PriorityQueue<>();
		
		pta.init(blue::add);
		
		BlueStateRef<Boolean> qbRef;
		while ((qbRef = blue.poll()) != null) {
			PTAState<Boolean> qb = qbRef.deref();
			
			Stream<PTAState<Boolean>> stream = (parallel) ?
					pta.redStates.parallelStream() : pta.redStates.stream();
			Stream<RedBlueMerge<Boolean>> filtered = stream.map(qr -> pta.tryMerge(qr, qb))
					.filter(m -> m != null);
			
			Optional<RedBlueMerge<Boolean>> result = (deterministic) ?
					filtered.findFirst() : filtered.findAny();

			if (result.isPresent()) {
				RedBlueMerge<Boolean> mod = result.get();
				mod.apply(pta, blue::add);
			}
			else {
				pta.promote(qb, blue::add);
			}
		}
		
		return ptaToDFA(pta);
	}
	
	private CompactDFA<I> ptaToDFA(PTA<Boolean> pta) {
		CompactDFA<I> result = new CompactDFA<>(alphabet, pta.getNumRedStates() + 1);
		pta.toAutomaton(result, alphabet, true);
		return result;
	}

}
