/* Copyright (C) 2013-2018 TU Dortmund
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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import de.learnlib.datastructure.pta.pta.BlueFringePTA;
import de.learnlib.datastructure.pta.pta.BlueFringePTAState;
import de.learnlib.datastructure.pta.pta.PTATransition;
import de.learnlib.datastructure.pta.pta.RedBlueMerge;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;

/**
 * A state-merging learning algorithm based on the evidence principle. On an operational level this algorithm is very
 * similar to the {@link BlueFringeRPNIDFA} algorithm. However, whereas the basic RPNI approach merges the very first
 * pair of nodes that resemble a valid merge, the EDSM variant prioritizes the promotion of states (to be unmergable)
 * and only proceeds to merge states, if there exists at least one mergable blue state for every red state. If such a
 * situation occurs, the algorithm merges the two states whose merge would yield the biggest score (see {@link
 * EDSMUtil#score(UniversalDeterministicAutomaton, List, List)}). Thus the behavior of this algorithm is more passive,
 * or as the name suggest evidence-driven.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class BlueFringeEDSMDFA<I> extends BlueFringeRPNIDFA<I> {

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
    public DFA<?, I> computeModel() {
        BlueFringePTA<Boolean, Void> pta = new BlueFringePTA<>(super.alphabetSize);
        initializePTA(pta);

        Set<PTATransition<BlueFringePTAState<Boolean, Void>>> blue = new HashSet<>();

        pta.init(blue::add);

        while (!blue.isEmpty()) {
            boolean promotion = false;
            RedBlueMerge<Boolean, Void, BlueFringePTAState<Boolean, Void>> bestMerge = null;
            PTATransition<BlueFringePTAState<Boolean, Void>> bestTransition = null;
            long bestScore = Long.MIN_VALUE;

            final Iterator<PTATransition<BlueFringePTAState<Boolean, Void>>> blueIter = blue.iterator();

            while (blueIter.hasNext()) {
                final PTATransition<BlueFringePTAState<Boolean, Void>> qbRef = blueIter.next();
                final BlueFringePTAState<Boolean, Void> qb = qbRef.getTarget();

                Stream<BlueFringePTAState<Boolean, Void>> stream = pta.redStatesStream();
                if (super.parallel) {
                    stream = stream.parallel();
                }

                final Optional<Pair<RedBlueMerge<Boolean, Void, BlueFringePTAState<Boolean, Void>>, Long>> result =
                        stream.map(qr -> tryMerge(pta, qr, qb))
                              .filter(Objects::nonNull)
                              .map(merge -> new Pair<>(merge,
                                                       EDSMUtil.score(merge.toMergedAutomaton(),
                                                                      super.positive,
                                                                      super.negative)))
                              .max(Comparator.comparingLong(Pair::getSecond));

                if (result.isPresent()) {
                    final Pair<RedBlueMerge<Boolean, Void, BlueFringePTAState<Boolean, Void>>, Long> mergeResult =
                            result.get();

                    if (mergeResult.getSecond() > bestScore) {
                        bestMerge = mergeResult.getFirst();
                        bestTransition = qbRef;
                    }
                } else {
                    promotion = true;
                    blueIter.remove();
                    pta.promote(qb, blue::add);
                    break;
                }
            }
            if (!promotion) {
                blue.remove(bestTransition);
                bestMerge.apply(pta, blue::add);
            }
        }

        return ptaToModel(pta);
    }
}