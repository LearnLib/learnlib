package de.learnlib.eqtests.basic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.comparison.CmpUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public class WpMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I,O>,I,O>
		implements EquivalenceOracle<A, I, O> {
	
	private final int maxDepth;
	private final MembershipOracle<I, O> sulOracle;
	
	public WpMethodEQOracle(int maxDepth, MembershipOracle<I,O> sulOracle) {
		this.maxDepth = maxDepth;
		this.sulOracle = sulOracle;
	}

	@Override
	public DefaultQuery<I, O> findCounterExample(A hypothesis,
			Alphabet<I> alphabet) {
		UniversalDeterministicAutomaton<?, I, ?, ?, ?> aut = hypothesis;
		Output<I,O> out = hypothesis;
		return doFindCounterExample(aut, out, alphabet);
	}
	
	
	private <S> DefaultQuery<I,O> doFindCounterExample(UniversalDeterministicAutomaton<S, I, ?, ?, ?> hypothesis,
			Output<I,O> output, Collection<? extends I> inputs) {
		
		List<Word<I>> stateCover = Automata.stateCover(hypothesis, inputs);
		
		WordBuilder<I> wb = new WordBuilder<>();
		
		for(Word<I> as : stateCover) {
			S state = hypothesis.getState(as);
			List<Word<I>> charSuffixes = Automata.stateCharacterizingSet(hypothesis, inputs, state);
			if(charSuffixes.isEmpty())
				charSuffixes = Collections.singletonList(Word.<I>epsilon());
			
			for(Word<I> suffix : charSuffixes) {
				for(List<? extends I> middle : CollectionsUtil.allTuples(inputs, 1, maxDepth)) {
					wb.append(as).append(middle).append(suffix);
					Word<I> queryWord = wb.toWord();
					wb.clear();
					DefaultQuery<I,O> query = new DefaultQuery<>(queryWord);
					O hypOutput = output.computeOutput(queryWord);
					sulOracle.processQueries(Collections.singleton(query));
					if(!CmpUtil.equals(hypOutput, query.getOutput()))
						return query;
				}
			}
		}
		
		return null;
	}

}
