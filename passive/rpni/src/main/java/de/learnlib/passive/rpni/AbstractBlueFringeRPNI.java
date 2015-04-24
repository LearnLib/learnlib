package de.learnlib.passive.rpni;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;

public abstract class AbstractBlueFringeRPNI<I,D,SP,TP,M> implements PassiveLearningAlgorithm<M, I, D> {
	
	protected final Alphabet<I> alphabet;
	protected final int alphabetSize;
	private final List<int[]> positive = new ArrayList<>();
	private final List<int[]> negative = new ArrayList<>();
	
	private boolean parallel = true;
	private boolean deterministic = false;

	public AbstractBlueFringeRPNI(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
		this.alphabetSize = alphabet.size();
	}

	@Override
	public M computeModel() {
		BlueFringePTA<SP,TP> pta = new BlueFringePTA<>(alphabetSize);
		initializePTA(pta);
				
		Queue<BlueStateRef<BlueFringePTAState<SP,TP>>> blue = new PriorityQueue<>();
		
		pta.init(blue::add);
		
		BlueStateRef<BlueFringePTAState<SP,TP>> qbRef;
		while ((qbRef = blue.poll()) != null) {
			BlueFringePTAState<SP,TP> qb = qbRef.deref();
			
			Stream<BlueFringePTAState<SP,TP>> stream = (parallel) ?
					pta.redStates.parallelStream() : pta.redStates.stream();
			Stream<RedBlueMerge<SP,TP,BlueFringePTAState<SP,TP>>> filtered = stream.map(qr -> pta.tryMerge(qr, qb))
					.filter(m -> m != null);
			
			Optional<RedBlueMerge<SP,TP,BlueFringePTAState<SP,TP>>> result = (deterministic) ?
					filtered.findFirst() : filtered.findAny();

			if (result.isPresent()) {
				RedBlueMerge<SP,TP,BlueFringePTAState<SP,TP>> mod = result.get();
				mod.apply(pta, blue::add);
			}
			else {
				pta.promote(qb, blue::add);
			}
		}
		
		return ptaToModel(pta);
	}
	
	protected abstract void initializePTA(BlueFringePTA<SP, TP> pta);
	protected abstract M ptaToModel(BlueFringePTA<SP, TP> pta);
	
}
