/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithm.rpni;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import de.learnlib.algorithm.PassiveLearningAlgorithm;
import de.learnlib.datastructure.pta.BlueFringePTA;
import de.learnlib.datastructure.pta.BlueFringePTAState;
import de.learnlib.datastructure.pta.PTATransition;
import de.learnlib.datastructure.pta.RedBlueMerge;
import de.learnlib.datastructure.pta.config.DefaultProcessingOrders;
import de.learnlib.datastructure.pta.config.ProcessingOrder;
import net.automatalib.alphabet.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstract base class for Blue-Fringe-RPNI algorithms.
 * <p>
 * Unlike most descriptions of RPNI in the literature, the Blue Fringe version of RPNI does not consider all possible
 * pairs of states for merging, but instead maintains a monotonically growing set of "red states", the immediate non-red
 * successors of which are called blue states. In each iteration of the main loop, an attempt is made to merge a blue
 * state into any red state. If this is impossible, the blue state is promoted, meaning it is converted into a red state
 * itself. The procedure terminates when all states are red.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 * @param <M>
 *         model type
 */
public abstract class AbstractBlueFringeRPNI<I, D, SP, TP, M> implements PassiveLearningAlgorithm<M, I, D> {

    protected final Alphabet<I> alphabet;
    protected final int alphabetSize;

    private ProcessingOrder order = DefaultProcessingOrders.CANONICAL_ORDER;
    private boolean parallel;
    private boolean deterministic;

    /**
     * Constructor.
     *
     * @param alphabet
     *         the alphabet
     */
    public AbstractBlueFringeRPNI(Alphabet<I> alphabet) {
        this.alphabet = alphabet;
        this.alphabetSize = alphabet.size();
    }

    /**
     * Sets whether attempts to merge a blue into a red state are conducted in parallel.
     * <p>
     * Note that setting this to {@code true} does not inhibit the possibility of deterministic algorithm runs (see
     * {@link #setDeterministic(boolean)}).
     *
     * @param parallel
     *         whether to parallelize the process of finding a possible merge
     */
    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    /**
     * Sets whether the outcome of the algorithm is required to be deterministic (i.e., subsequent calls of {@link
     * #computeModel()} on the same input data will perform the same merges and return the same result).
     * <p>
     * Note that if parallel execution is disabled (see {@link #setParallel(boolean)}), the algorithm will most likely
     * (but is not required to) behave deterministically even with this set to {@code false}. However, if
     * parallelization is enabled, results of subsequent invocations will most likely differ with this parameter set to
     * {@code false}.
     *
     * @param deterministic
     *         whether to enforce deterministic algorithm behavior
     */
    public void setDeterministic(boolean deterministic) {
        this.deterministic = deterministic;
    }

    /**
     * Sets the order in which the respective merge candidates should be processed.
     *
     * @param order
     *         the order
     */
    public void setProcessingOrder(ProcessingOrder order) {
        this.order = order;
    }

    @Override
    public M computeModel() {
        final BlueFringePTA<SP, TP> pta = fetchPTA();
        final Queue<PTATransition<BlueFringePTAState<SP, TP>>> blue = order.createWorklist();

        pta.init(blue::offer);

        PTATransition<BlueFringePTAState<SP, TP>> qbRef;
        while ((qbRef = blue.poll()) != null) {
            BlueFringePTAState<SP, TP> qb = qbRef.getTarget();
            assert qb != null;

            final List<BlueFringePTAState<SP, TP>> redStates = pta.getRedStates();
            final Stream<BlueFringePTAState<SP, TP>> stream =
                    parallel ? redStates.parallelStream() : redStates.stream();

            @SuppressWarnings("nullness") // we filter the null merges
            final Stream<RedBlueMerge<BlueFringePTAState<SP, TP>, SP, TP>> possibleMerges =
                    stream.map(qr -> tryMerge(pta, qr, qb)).filter(Objects::nonNull);
            final Stream<RedBlueMerge<BlueFringePTAState<SP, TP>, SP, TP>> filteredMerges =
                    selectMerges(possibleMerges);
            final Optional<RedBlueMerge<BlueFringePTAState<SP, TP>, SP, TP>> result =
                    deterministic ? filteredMerges.findFirst() : filteredMerges.findAny();

            if (result.isPresent()) {
                RedBlueMerge<BlueFringePTAState<SP, TP>, SP, TP> mod = result.get();
                mod.apply(pta, blue::offer);
            } else {
                pta.promote(qb, blue::offer);
            }
        }

        return ptaToModel(pta);
    }

    /**
     * Fetches the initial {@link BlueFringePTA PTA} for model construction. If subclasses need to cache the training
     * data this may be a fresh instance. If subclasses directly insert training data to a local PTA, they should make
     * sure that repeated invocations of this method are not possible.
     *
     * @return the {@link BlueFringePTA PTA} for model construction.
     */
    protected abstract BlueFringePTA<SP, TP> fetchPTA();

    /**
     * Attempts to merge a blue state into a red state.
     *
     * @param pta
     *         the blue fringe PTA
     * @param qr
     *         the red state (i.e., the merge target)
     * @param qb
     *         the blue state (i.e., the merge source)
     *
     * @return a valid {@link RedBlueMerge} object representing a possible merge of {@code qb} into {@code qr}, or
     * {@code null} if the merge is impossible
     */
    protected @Nullable RedBlueMerge<BlueFringePTAState<SP, TP>, SP, TP> tryMerge(BlueFringePTA<SP, TP> pta,
                                                                                  BlueFringePTAState<SP, TP> qr,
                                                                                  BlueFringePTAState<SP, TP> qb) {
        return pta.tryMerge(qr, qb);
    }

    /**
     * Transforms the final PTA into a model.
     *
     * @param pta
     *         the final PTA
     *
     * @return a model built from the final PTA
     */
    protected abstract M ptaToModel(BlueFringePTA<SP, TP> pta);

    /**
     * Implementing the method allows subclasses to decide on (and possibly reject) valid merges.
     *
     * @param merges
     *         the prosed (valid) merges
     *
     * @return the merges that should be considered for selecting a merge.
     */
    protected Stream<RedBlueMerge<BlueFringePTAState<SP, TP>, SP, TP>> selectMerges(Stream<RedBlueMerge<BlueFringePTAState<SP, TP>, SP, TP>> merges) {
        // by default, we are greedy and try to merge the first merge
        return merges;
    }

}
