package de.learnlib.algorithms.discriminationtree.dfa;


import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.algorithms.discriminationtree.AbstractDTLearner;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;
import de.learnlib.api.LearningAlgorithm.DFALearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.oracles.AbstractQuery;

/**
 * Algorithm for learning DFA using the Discrimination Tree algorithm.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 */
public class DTLearnerDFA<I> extends AbstractDTLearner<DFA<?,I>, I, Boolean, Boolean, Void> implements DFALearner<I> {
	
	private final HypothesisWrapperDFA<I> hypWrapper;

	/**
	 * Constructor.
	 * @param alphabet the input alphabet
	 * @param oracle the membership oracle
	 * @param suffixFinder method to use for analyzing counterexamples
	 */
	@GenerateBuilder(defaults = AbstractDTLearner.BuilderDefaults.class)
	public DTLearnerDFA(Alphabet<I> alphabet,
			MembershipOracle<I, Boolean> oracle,
			LocalSuffixFinder<? super I, ? super Boolean> suffixFinder) {
		super(alphabet, oracle, suffixFinder);
		this.hypWrapper = new HypothesisWrapperDFA<I>(hypothesis);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#getHypothesisModel()
	 */
	@Override
	public DFA<?, I> getHypothesisModel() {
		return hypWrapper;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.discriminationtree.AbstractDTLearner#spQuery(de.learnlib.algorithms.discriminationtree.hypothesis.HState)
	 */
	@Override
	protected Query<I, Boolean> spQuery(final HState<I, Boolean, Boolean, Void> state) {
		return new AbstractQuery<I,Boolean>(state.getAccessSequence(), Word.<I>epsilon()) {
			@Override
			public void answer(Boolean val) {
				state.setProperty(val);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.discriminationtree.AbstractDTLearner#tpQuery(de.learnlib.algorithms.discriminationtree.hypothesis.HTransition)
	 */
	@Override
	protected Query<I, Boolean> tpQuery(
			HTransition<I, Boolean, Boolean, Void> transition) {
		return null;
	}
}
