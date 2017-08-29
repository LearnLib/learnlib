package de.learnlib.eqtests.basic;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * Implements an equivalence test by applying the Wp-method test on the given hypothesis automaton,
 * as described in "Test Selection Based on Finite State Models" by S. Fujiwara et al.
 * Instead of enumerating the test suite in order, this is a sampling implementation:
 * 1. sample uniformly from the states for a prefix
 * 2. sample geometrically a random word
 * 3. sample a word from the set of suffixes / state identifiers (either local or global)
 * There are two parameters: minimalSize determines the minimal size of the random word, this is
 * useful when one first performs a W(p)-method with some depth and continue with this randomized
 * tester from that depth onward. The second parameter rndLength determines the expected length
 * of the random word. (The expected length in effect is minimalSize + rndLength.)
 * In the unbounded case it will not terminate for a correct hypothesis.
 *
 * @param <A> automaton type
 * @param <I> input symbol type
 * @param <D> output domain type
 * @author Joshua Moerman
 */
public class RandomWpMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
        implements EquivalenceOracle<A, I, D> {
    private final MembershipOracle<I, D> sulOracle;
    private final int minimalSize;
    private final int rndLength;
    private final int bound;

    /**
     * Constructor for an unbounded testing oracle
     *
     * @param sulOracle   oracle which answers tests.
     * @param minimalSize minimal size of the random word
     * @param rndLength   expected length (in addition to minimalSize) of random word
     */
    public RandomWpMethodEQOracle(MembershipOracle<I, D> sulOracle, int minimalSize, int rndLength) {
        this.sulOracle = sulOracle;
        this.minimalSize = minimalSize;
        this.rndLength = rndLength;
        this.bound = 0;
    }

    /**
     * Constructor for a bounded testing oracle
     *
     * @param sulOracle   oracle which answers tests.
     * @param minimalSize minimal size of the random word
     * @param rndLength   expected length (in addition to minimalSize) of random word
     * @param bound       specifies the bound (set to 0 for unbounded).
     */
    public RandomWpMethodEQOracle(MembershipOracle<I, D> sulOracle, int minimalSize, int rndLength, int bound) {
        this.sulOracle = sulOracle;
        this.minimalSize = minimalSize;
        this.rndLength = rndLength;
        this.bound = bound;
    }

    /*
     * (non-Javadoc)
     * @see de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object, java.util.Collection)
     */
    @Override
    @ParametersAreNonnullByDefault
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        UniversalDeterministicAutomaton<?, I, ?, ?, ?> aut = hypothesis;
        Output<I, D> out = hypothesis;
        return doFindCounterExample(aut, out, inputs);
    }

    /*
     * Delegate target, used to bind the state-parameter of the automaton
     */
    private <S> DefaultQuery<I, D> doFindCounterExample(UniversalDeterministicAutomaton<S, I, ?, ?, ?> hypothesis,
                                                        Output<I, D> output, Collection<? extends I> inputs) {
        // Note that we want to use ArrayLists because we want constant time random access
        // We will sample from this for a prefix
        ArrayList<Word<I>> stateCover = new ArrayList<>(hypothesis.size());
        Automata.cover(hypothesis, inputs, stateCover, null);

        // Then repeatedly from this for a random word
        ArrayList<I> arrayAlphabet = new ArrayList<>(inputs);

        // Finally we test the state with a suffix, sometimes a global one, sometimes local
        ArrayList<Word<I>> globalSuffixes = new ArrayList<>();
        Automata.characterizingSet(hypothesis, inputs, globalSuffixes);

        MutableMapping<S, ArrayList<Word<I>>> localSuffixSets = hypothesis.createStaticStateMapping();
        for (S state : hypothesis.getStates()) {
            ArrayList<Word<I>> suffixSet = new ArrayList<>();
            Automata.stateCharacterizingSet(hypothesis, inputs, state, suffixSet);
            localSuffixSets.put(state, suffixSet);
        }

        Random rand = new Random();
        int currentBound = bound;
        while (bound == 0 || currentBound-- > 0) {
            WordBuilder<I> wb = new WordBuilder<>(minimalSize + rndLength + 1);

            // pick a random state
            wb.append(stateCover.get(rand.nextInt(stateCover.size())));

            // construct random middle part (of some expected length)
            int size = minimalSize;
            while ((size > 0) || (rand.nextDouble() > 1 / (rndLength + 1.0))) {
                wb.append(arrayAlphabet.get(rand.nextInt(arrayAlphabet.size())));
                if (size > 0) size--;
            }

            // pick a random suffix for this state
            // 50% chance for state testing, 50% chance for transition testing
            if (rand.nextBoolean()) {
                // global
                if (!globalSuffixes.isEmpty()) {
                    wb.append(globalSuffixes.get(rand.nextInt(globalSuffixes.size())));
                }
            } else {
                // local
                S state2 = hypothesis.getState(wb);
                ArrayList<Word<I>> localSuffixes = localSuffixSets.get(state2);
                if (!localSuffixes.isEmpty()) {
                    wb.append(localSuffixes.get(rand.nextInt(localSuffixes.size())));
                }
            }

            Word<I> queryWord = wb.toWord();
            DefaultQuery<I, D> query = new DefaultQuery<>(queryWord);
            D hypOutput = output.computeOutput(queryWord);
            sulOracle.processQueries(Collections.singleton(query));
            if (!Objects.equals(hypOutput, query.getOutput()))
                return query;
        }

        // no counter example found within the bound
        return null;
    }
}
