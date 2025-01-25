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
package de.learnlib.algorithm.adt.config.model.extender;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.api.ADTExtender;
import de.learnlib.algorithm.adt.api.PartialTransitionAnalyzer;
import de.learnlib.algorithm.adt.automaton.ADTHypothesis;
import de.learnlib.algorithm.adt.automaton.ADTState;
import de.learnlib.algorithm.adt.config.model.DefensiveADSCalculator;
import de.learnlib.algorithm.adt.model.ExtensionResult;
import de.learnlib.algorithm.adt.util.ADTUtil;
import de.learnlib.query.DefaultQuery;
import net.automatalib.common.smartcollection.ReflexiveMapView;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;

public class DefaultExtender implements ADTExtender {

    private final DefensiveADSCalculator adsCalculator;

    public DefaultExtender(DefensiveADSCalculator adsCalculator) {
        this.adsCalculator = adsCalculator;
    }

    @Override
    public <I, O> ExtensionResult<ADTState<I, O>, I, O> computeExtension(ADTHypothesis<I, O> hypothesis,
                                                                         PartialTransitionAnalyzer<ADTState<I, O>, I> partialTransitionAnalyzer,
                                                                         ADTNode<ADTState<I, O>, I, O> ads) {
        // cannot compute extension for root node
        final ADTNode<ADTState<I, O>, I, O> parent = ads.getParent();
        if (parent == null) {
            return ExtensionResult.empty();
        }

        final Set<ADTNode<ADTState<I, O>, I, O>> initialNodes = ADTUtil.collectLeaves(ads);

        // currently can't handle more than two initial nodes
        if (initialNodes.size() > 2) {
            return ExtensionResult.empty();
        }

        final Pair<Word<I>, Word<O>> parentTrace = ADTUtil.buildTraceForNode(parent);

        // as long as we encounter exceptions, repeat
        while (true) {
            final Word<I> inputTrace = parentTrace.getFirst();
            final Word<O> outputTrace = parentTrace.getSecond();

            try {
                Map<ADTState<I, O>, ADTState<I, O>> currentToInitialMapping =
                        new ReflexiveMapView<>(ADTUtil.collectHypothesisStates(ads));

                // apply parent trace
                for (int idx = 0; idx < inputTrace.length(); idx++) {

                    final Map<ADTState<I, O>, ADTState<I, O>> nextCurrentToInitialMapping = new LinkedHashMap<>();
                    final I input = inputTrace.getSymbol(idx);
                    final O output = outputTrace.getSymbol(idx);

                    for (Map.Entry<ADTState<I, O>, ADTState<I, O>> entry : currentToInitialMapping.entrySet()) {
                        final ADTState<I, O> s = entry.getKey();
                        if (!partialTransitionAnalyzer.isTransitionDefined(s, input)) {
                            partialTransitionAnalyzer.closeTransition(s, input);
                        }

                        if (!Objects.equals(hypothesis.getOutput(s, input), output)) {
                            final ADTState<I, O> initial = entry.getValue();
                            final Word<I> as = initial.getAccessSequence();
                            final Word<O> asOut = hypothesis.computeOutput(as);
                            final Word<I> traceIn = inputTrace.prefix(idx + 1);
                            final Word<O> traceOut = outputTrace.prefix(idx + 1);

                            final DefaultQuery<I, Word<O>> ce =
                                    new DefaultQuery<>(as.concat(traceIn), asOut.concat(traceOut));

                            return new ExtensionResult<>(ce);
                        }

                        final ADTState<I, O> successor = hypothesis.getSuccessor(s, input);

                        // converging states, cannot distinguish
                        if (nextCurrentToInitialMapping.containsKey(successor)) {
                            return ExtensionResult.empty();
                        }

                        nextCurrentToInitialMapping.put(successor, currentToInitialMapping.get(s));
                    }

                    currentToInitialMapping = nextCurrentToInitialMapping;
                }

                final Set<ADTState<I, O>> currentSet = currentToInitialMapping.keySet();
                final Optional<ADTNode<ADTState<I, O>, I, O>> potentialExtension = adsCalculator.compute(hypothesis,
                                                                                                         hypothesis.getInputAlphabet(),
                                                                                                         currentSet,
                                                                                                         partialTransitionAnalyzer);

                if (!potentialExtension.isPresent()) {
                    return ExtensionResult.empty();
                }

                final ADTNode<ADTState<I, O>, I, O> extension = potentialExtension.get();

                // set the original initial states
                for (ADTNode<ADTState<I, O>, I, O> finalNode : ADTUtil.collectLeaves(extension)) {
                    finalNode.setState(currentToInitialMapping.get(finalNode.getState()));
                }

                return new ExtensionResult<>(extension);
            } catch (PartialTransitionAnalyzer.HypothesisModificationException ignored) {
                //do nothing, repeat
            }
        }
    }

}
