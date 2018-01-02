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
package de.learnlib.algorithms.adt.config.model.extender;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.adt.adt.ADTNode;
import de.learnlib.algorithms.adt.api.ADTExtender;
import de.learnlib.algorithms.adt.api.PartialTransitionAnalyzer;
import de.learnlib.algorithms.adt.automaton.ADTHypothesis;
import de.learnlib.algorithms.adt.automaton.ADTState;
import de.learnlib.algorithms.adt.config.model.DefensiveADSCalculator;
import de.learnlib.algorithms.adt.model.ExtensionResult;
import de.learnlib.algorithms.adt.util.ADTUtil;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

/**
 * @author frohme
 */
@ParametersAreNonnullByDefault
public class DefaultExtender implements ADTExtender {

    private final DefensiveADSCalculator adsCalculator;

    public DefaultExtender(final DefensiveADSCalculator adsCalculator) {
        this.adsCalculator = adsCalculator;
    }

    @Override
    public <I, O> ExtensionResult<ADTState<I, O>, I, O> computeExtension(final ADTHypothesis<I, O> hypothesis,
                                                                         final PartialTransitionAnalyzer<ADTState<I, O>, I> partialTransitionAnalyzer,
                                                                         final ADTNode<ADTState<I, O>, I, O> ads) {
        // cannot compute extension for root node
        if (ads.getParent() == null) {
            return ExtensionResult.empty();
        }

        final Set<ADTNode<ADTState<I, O>, I, O>> initialNodes = ADTUtil.collectLeaves(ads);

        // currently can't handle more than two initial nodes
        if (initialNodes.size() > 2) {
            return ExtensionResult.empty();
        }

        final Pair<Word<I>, Word<O>> parentTrace = ADTUtil.buildTraceForNode(ads.getParent());

        // as long as we encounter exceptions, repeat
        while (true) {
            final Word<I> inputTrace = parentTrace.getFirst();
            final Word<O> outputTrace = parentTrace.getSecond();

            try {
                Map<ADTState<I, O>, ADTState<I, O>> currentToInitialMapping = ADTUtil.collectLeaves(ads)
                                                                                     .stream()
                                                                                     .map(ADTNode::getHypothesisState)
                                                                                     .collect(Collectors.toMap(Function.identity(),
                                                                                                               Function.identity()));

                // apply parent trace
                for (int idx = 0; idx < inputTrace.length(); idx++) {

                    final Map<ADTState<I, O>, ADTState<I, O>> nextCurrentToInitialMapping = new HashMap<>();
                    final I input = inputTrace.getSymbol(idx);
                    final O output = outputTrace.getSymbol(idx);

                    for (final Map.Entry<ADTState<I, O>, ADTState<I, O>> entry : currentToInitialMapping.entrySet()) {
                        final ADTState<I, O> s = entry.getKey();
                        if (!partialTransitionAnalyzer.isTransitionDefined(s, input)) {
                            partialTransitionAnalyzer.closeTransition(s, input);
                        }

                        if (!hypothesis.getOutput(s, input).equals(output)) {
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

                // set the original intial states
                for (ADTNode<ADTState<I, O>, I, O> finalNode : ADTUtil.collectLeaves(extension)) {
                    finalNode.setHypothesisState(currentToInitialMapping.get(finalNode.getHypothesisState()));
                }

                return new ExtensionResult<>(extension);
            } catch (PartialTransitionAnalyzer.HypothesisModificationException hme) {
                //do nothing, repeat
            }
        }
    }

}
