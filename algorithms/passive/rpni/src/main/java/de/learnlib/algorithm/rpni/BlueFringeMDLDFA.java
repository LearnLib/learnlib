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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import de.learnlib.algorithm.PassiveLearningAlgorithm;
import de.learnlib.algorithm.PassiveLearningAlgorithm.PassiveDFALearner;
import de.learnlib.datastructure.pta.BlueFringePTA;
import de.learnlib.datastructure.pta.BlueFringePTAState;
import de.learnlib.datastructure.pta.RedBlueMerge;
import de.learnlib.datastructure.pta.wrapper.DFAWrapper;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.common.smartcollection.IntSeq;

/**
 * A state-merging learning algorithm based on the minimal description length principle. On an operational level this
 * algorithm is very similar to the {@link BlueFringeRPNIDFA} algorithm. However, whereas the basic RPNI approach merges
 * the very first pair of nodes that resemble a valid merge, the MDL variant computes an additional score and only
 * commits to a merge, if the resulting hypothesis will yield a better score.
 * <p>
 * This passive approach to state-merging works better in scenarios where only positive training data is available.
 * Hence, this algorithm only expect positive training data.
 * <p>
 * <b>Implementation note:</b> This implementation does support repeated calls to {@link
 * PassiveLearningAlgorithm#computeModel()}.
 *
 * @param <I>
 *         input symbol type
 */
public class BlueFringeMDLDFA<I> extends AbstractBlueFringeRPNI<I, Boolean, Boolean, Void, DFA<?, I>>
        implements PassiveDFALearner<I> {

    private final List<IntSeq> positive = new ArrayList<>();

    private double currentScore = Double.POSITIVE_INFINITY;

    /**
     * Constructor.
     *
     * @param alphabet
     *         the alphabet
     */
    public BlueFringeMDLDFA(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public void addSamples(Collection<? extends DefaultQuery<I, Boolean>> samples) {
        for (DefaultQuery<I, Boolean> query : samples) {
            if (!query.getOutput()) {
                throw new IllegalArgumentException("Only positive examples are allowed");
            }
            positive.add(query.getInput().asIntSeq(alphabet));
        }
    }

    @Override
    public DFA<?, I> computeModel() {
        // disable parallelism because our track-keeping of the current score is not thread-safe
        super.setDeterministic(true);
        super.setParallel(false);
        return super.computeModel();
    }

    @Override
    protected BlueFringePTA<Boolean, Void> fetchPTA() {
        final BlueFringePTA<Boolean, Void> pta = new BlueFringePTA<>(alphabet.size());

        for (IntSeq pos : positive) {
            pta.addSample(pos, true);
        }

        return pta;
    }

    @Override
    protected Stream<RedBlueMerge<BlueFringePTAState<Boolean, Void>, Boolean, Void>> selectMerges(Stream<RedBlueMerge<BlueFringePTAState<Boolean, Void>, Boolean, Void>> merges) {
        return merges.filter(this::decideOnValidMerge);
    }

    private boolean decideOnValidMerge(RedBlueMerge<BlueFringePTAState<Boolean, Void>, Boolean, Void> merge) {
        final double score = MDLUtil.score(merge.toMergedAutomaton(), super.alphabetSize, positive);
        if (score < currentScore) {
            currentScore = score;
            return true;
        }

        return false;
    }

    @Override
    protected DFA<?, I> ptaToModel(BlueFringePTA<Boolean, Void> pta) {
        return new DFAWrapper<>(alphabet, pta);
    }
}
