package de.learnlib.oracle.equivalence;

import de.learnlib.oracle.MembershipOracle;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Stream;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator.Method;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator.Optimize;
import net.automatalib.word.Word;

/**
 * This Equivalence oracle selects test cases based on k-way transitions coverage. It does that by generating random
 * queries and finding the smallest subset with the highest coverage. In other words, this oracle finds counter examples
 * by running random paths that cover all pairwise / k-way transitions.
 */
public class KWayTransitionCoverageEqOracle<A extends UniversalDeterministicAutomaton<S, I, T, ?, ?> & Output<I, D>, S, I, T, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private final int k;
    private final Method method;
    private final int numGeneratePaths;
    private final int maxPathLen;
    private final int maxNumberOfSteps;
    private final Optimize optimize;
    private final int randomWalkLen;
    private final Random random;

    /**
     * Initializes the KWayTransitionCoverageEqOracle.
     *
     * @param batchSize
     *         batch size
     * @param oracle
     *         system under learning
     * @param random
     *         random
     * @param k
     *         k value used for K-Way transitions, i.e., number of steps between start and end of a transition
     * @param method
     *         defines how the queries are generated 'random' or 'prefix'
     * @param numGeneratePaths
     *         number of random queries used to find the optimal subset
     * @param maxPathLen
     *         maximum step size of a generated path
     * @param maxNumberOfSteps
     *         maximum number of steps executed on the SUL (0 = no limit)
     * @param optimize
     *         minimize either the number of 'steps' or 'queries' executed
     * @param randomWalkLen
     *         number of steps added by 'prefix' generated paths
     */
    public KWayTransitionCoverageEqOracle(MembershipOracle<I, D> oracle,
                                          Random random,
                                          int batchSize,
                                          int k,
                                          Method method,
                                          int numGeneratePaths,
                                          int maxPathLen,
                                          int maxNumberOfSteps,
                                          Optimize optimize,
                                          int randomWalkLen) {
        super(oracle, batchSize);
        assert k > 1;

        this.random = random;
        this.k = k;
        this.method = method;
        this.numGeneratePaths = numGeneratePaths;
        this.maxPathLen = maxPathLen;
        this.maxNumberOfSteps = maxNumberOfSteps;
        this.optimize = optimize;
        this.randomWalkLen = randomWalkLen;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        return IteratorUtil.stream(new KWayTransitionCoverTestsIterator<>(hypothesis,
                                                                          inputs,
                                                                          random,
                                                                          k,
                                                                          method,
                                                                          numGeneratePaths,
                                                                          maxPathLen,
                                                                          maxNumberOfSteps,
                                                                          optimize,
                                                                          randomWalkLen));
    }
}
