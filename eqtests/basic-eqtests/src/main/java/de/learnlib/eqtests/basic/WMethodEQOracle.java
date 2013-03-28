package de.learnlib.eqtests.basic;

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

public class WMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?,?> & Output<I,O>, I, O>
	implements EquivalenceOracle<A, I, O> {
	
	private int maxDepth;
	private final MembershipOracle<I,O> sulOracle;
	
	public WMethodEQOracle(int maxDepth, MembershipOracle<I,O> sulOracle) {
		this.maxDepth = maxDepth;
		this.sulOracle = sulOracle;
	}

	@Override
	public DefaultQuery<I, O> findCounterExample(A hypothesis,
			Alphabet<I> alphabet) {
		
		List<Word<I>> transCover = Automata.transitionCover(hypothesis, alphabet);
		List<Word<I>> charSuffixes = Automata.characterizingSet(hypothesis, alphabet);
		
		// Special case: List of characterizing suffixes may be empty,
		// but in this case we still need to test!
		if(charSuffixes.isEmpty())
			charSuffixes = Collections.singletonList(Word.<I>epsilon());
		
		
		WordBuilder<I> wb = new WordBuilder<>();
		
		for(List<I> middle : CollectionsUtil.allTuples(alphabet, 1, maxDepth)) {
			for(Word<I> trans : transCover) {
				for(Word<I> suffix : charSuffixes) {
					wb.append(trans).append(middle).append(suffix);
					Word<I> queryWord = wb.toWord();
					wb.clear();
					DefaultQuery<I,O> query = new DefaultQuery<>(queryWord);
					O hypOutput = hypothesis.computeOutput(queryWord);
					sulOracle.processQueries(Collections.singleton(query));
					if(!CmpUtil.equals(hypOutput, query.getOutput()))
						return query;
				}
			}
		}
		
		return null;
	}
	
}