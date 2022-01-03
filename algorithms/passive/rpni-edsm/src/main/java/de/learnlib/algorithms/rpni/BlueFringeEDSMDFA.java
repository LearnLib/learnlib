/* Copyright (C) 2013-2022 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package de.learnlib.algorithms.rpni;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import de.learnlib.api.algorithm.PassiveLearningAlgorithm;
import de.learnlib.api.algorithm.PassiveLearningAlgorithm.PassiveDFALearner;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.pta.PTAUtil;
import de.learnlib.datastructure.pta.pta.BlueFringePTA;
import de.learnlib.datastructure.pta.pta.BlueFringePTAState;
import de.learnlib.datastructure.pta.pta.RedBlueMerge;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.smartcollections.IntSeq;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * A state-merging learning algorithm based on the evidence principle. On an operational level this algorithm is very
 * similar to the {@link BlueFringeRPNIDFA} algorithm. However, whereas the basic RPNI approach merges the very first
 * pair of nodes that resemble a valid merge, the EDSM variant prioritizes the promotion of states (to be unmergable)
 * and only proceeds to merge states, if there exists at least one mergable blue state for every red state. If such a
 * situation occurs, the algorithm merges the two states whose merge would yield the biggest score (see {@link
 * EDSMUtil#score(UniversalDeterministicAutomaton, List, List)}). Thus the behavior of this algorithm is more passive,
 * or as the name suggest evidence-driven.
 * <p>
 * <b>Implementation note:</b> This implementation does support repeated calls to {@link
 * PassiveLearningAlgorithm#computeModel()}.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
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
        final BlueFringePTA<Boolean, Void> pta = new BlueFringePTA<>(alphabetSize);

        for (IntSeq pos : positive) {
            pta.addSample(pos, true);
        }

        for (IntSeq neg : negative) {
            pta.addSample(neg, false);
        }

        return pta;
    }

    @Override
    protected Stream<RedBlueMerge<Boolean, Void, BlueFringePTAState<Boolean, Void>>> selectMerges(Stream<RedBlueMerge<Boolean, Void, BlueFringePTAState<Boolean, Void>>> merges) {
        return merges.map(merge -> Pair.of(merge, EDSMUtil.score(merge.toMergedAutomaton(), positive, negative)))
                     .sorted(Collections.reverseOrder(Comparator.comparingLong(Pair::getSecond)))
                     .map(Pair::getFirst);
    }

    @Override
    protected CompactDFA<I> ptaToModel(BlueFringePTA<Boolean, Void> pta) {
        return PTAUtil.toDFA(pta, alphabet);
    }
}