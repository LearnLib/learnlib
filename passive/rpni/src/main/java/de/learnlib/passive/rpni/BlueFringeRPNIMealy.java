package de.learnlib.passive.rpni;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.passive.api.PassiveMealyLearner;
import de.learnlib.passive.commons.pta.BlueFringePTA;

/**
 * Blue-fringe version of RPNI for inferring Mealy machines.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
public class BlueFringeRPNIMealy<I,O>
		extends AbstractBlueFringeRPNI<I, Word<O>, Void, O, MealyMachine<?,I,?,O>>
		implements PassiveMealyLearner<I,O> {
	
	private final List<Pair<int[],Word<O>>> samples
		= new ArrayList<>();

	public BlueFringeRPNIMealy(Alphabet<I> alphabet) {
		super(alphabet);
	}

	@Override
	public void addSamples(
			Collection<? extends DefaultQuery<I, Word<O>>> samples) {
		for (DefaultQuery<I,Word<O>> qry : samples) {
			this.samples.add(new Pair<>(qry.getInput().toIntArray(alphabet), qry.getOutput()));
		}
	}

	@Override
	protected void initializePTA(BlueFringePTA<Void, O> pta) {
		for (Pair<int[],Word<O>> sample : samples) {
			pta.addSampleWithTransitionProperties(sample.getFirst(), sample.getSecond().asList());
		}
	}

	@Override
	protected MealyMachine<?, I, ?, O> ptaToModel(BlueFringePTA<Void, O> pta) {
		CompactMealy<I, O> mealy = new CompactMealy<>(alphabet, pta.getNumRedStates());
		pta.toAutomaton(mealy, alphabet);
		return mealy;
	}

}
