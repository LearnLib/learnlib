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
import java.util.Collections;
import java.util.Comparator;
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
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.common.smartcollection.IntSeq;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;

/**
 * A state-merging learning algorithm based on the evidence principle. On an operational level this algorithm is very
 * similar to the {@link BlueFringeRPNIDFA} algorithm. However, whereas the basic RPNI approach merges the very first
 * pair of nodes that resemble a valid merge, the EDSM variant prioritizes the promotion of states (to be unmergable)
 * and only proceeds to merge states, if there exists at least one mergable blue state for every red state. If such a
 * situation occurs, the algorithm merges the two states whose merge would yield the biggest score (see {@link
 * EDSMUtil#score(UniversalDeterministicAutomaton, List, List)}). Thus, the behavior of this algorithm is more passive,
 * or as the name suggest evidence-driven.
 * <p>
 * <b>Implementation note:</b> This implementation does support repeated calls to {@link
 * PassiveLearningAlgorithm#computeModel()}.
 *
 * @param <I>
 *         input symbol type
 */
public class BlueFringeEDSMDFA<I> extends AbstractBlueFringeRPNI<I, Boolean, Boolean, Void, DFA<?, I>>
        implements PassiveDFALearner<I> {

    private final List<IntSeq> positive = new ArrayList<>();
    private final List<IntSeq> negative = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param alphabet
     *         the alphabet
     */
    public BlueFringeEDSMDFA(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public void addSamples(Collection<? extends DefaultQuery<I, Boolean>> samples) {
        for (DefaultQuery<I, Boolean> query : samples) {
            final Word<I> input = query.getInput();
            if (query.getOutput()) {
                positive.add(input.asIntSeq(alphabet));
            } else {
                negative.add(input.asIntSeq(alphabet));
            }
        }
    }

    @Override
    protected BlueFringePTA<Boolean, Void> fetchPTA() {
        final BlueFringePTA<Boolean, Void> pta = new BlueFringePTA<>(alphabet.size());

        for (IntSeq pos : positive) {
            pta.addSample(pos, true);
        }

        for (IntSeq neg : negative) {
            pta.addSample(neg, false);
        }

        return pta;
    }

    @Override
    protected Stream<RedBlueMerge<BlueFringePTAState<Boolean, Void>, Boolean, Void>> selectMerges(Stream<RedBlueMerge<BlueFringePTAState<Boolean, Void>, Boolean, Void>> merges) {
        return merges.map(merge -> Pair.of(merge, EDSMUtil.score(merge.toMergedAutomaton(), positive, negative)))
                     .sorted(Collections.reverseOrder(Comparator.comparingLong(Pair::getSecond)))
                     .map(Pair::getFirst);
    }

    @Override
    protected DFA<?, I> ptaToModel(BlueFringePTA<Boolean, Void> pta) {
        return new DFAWrapper<>(alphabet, pta);
    }
}
