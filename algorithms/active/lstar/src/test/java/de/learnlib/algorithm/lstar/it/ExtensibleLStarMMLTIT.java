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
package de.learnlib.algorithm.lstar.it;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import de.learnlib.algorithm.lstar.closing.ClosingStrategies;
import de.learnlib.algorithm.lstar.closing.ClosingStrategy;
import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLTBuilder;
import de.learnlib.algorithm.lstar.mmlt.filter.MMLTPerfectSymbolFilter;
import de.learnlib.algorithm.lstar.mmlt.filter.MMLTRandomSymbolFilter;
import de.learnlib.filter.SymbolFilter;
import de.learnlib.filter.symbol.AcceptAllSymbolFilter;
import de.learnlib.filter.symbol.CachedSymbolFilter;
import de.learnlib.filter.symbol.IgnoreAllSymbolFilter;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.testsupport.example.LearningExample.MMLTLearningExample;
import de.learnlib.testsupport.it.learner.AbstractMMLTLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MMLTLearnerVariantList;
import de.learnlib.time.MMLTModelParams;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.util.automaton.mmlt.MMLTs;
import net.automatalib.word.Word;
import org.testng.annotations.Test;

@Test
public class ExtensibleLStarMMLTIT extends AbstractMMLTLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             TimedQueryOracle<I, O> mqOracle,
                                             MMLTLearningExample<I, O> example,
                                             MMLTLearnerVariantList<I, O> variants) {

        var mmlt = example.getReferenceAutomaton();
        var counters = countTimers(mmlt);

        List<Word<TimedInput<I>>> suffixes = new ArrayList<>();
        alphabet.forEach(s -> suffixes.add(Word.fromLetter(TimedInput.input(s))));
        // Do not include TimeoutSymbol because we want to check analyzing and handling counterexamples with it
        // suffixes.add(Word.fromLetter(new TimeoutSymbol<>()));

        var builder = new ExtensibleLStarMMLTBuilder<I, O>().withAlphabet(alphabet)
                                                            .withModelParams(example.getParams())
                                                            .withTimeOracle(mqOracle)
                                                            .withInitialSuffixes(suffixes);

        var filters = Arrays.asList(new MMLTPerfectSymbolFilter<>(mmlt),
                                    new MMLTRandomSymbolFilter<>(mmlt, 0.1, new Random(42)),
                                    new IgnoreAllSymbolFilter<TimedInput<I>, InputSymbol<I>>(),
                                    new AcceptAllSymbolFilter<TimedInput<I>, InputSymbol<I>>());

        for (ClosingStrategy<? super TimedInput<I>, ? super Word<TimedOutput<O>>> strategy : ClosingStrategies.values()) {
            builder.setClosingStrategy(strategy);
            for (SymbolFilter<TimedInput<I>, InputSymbol<I>> filter : filters) {

                var cachedFilter = new CachedSymbolFilter<>(filter); // need to wrap to enable updates to responses
                var learner = builder.withSymbolFilter(cachedFilter).create();
                variants.addLearnerVariant("system=" + example + ",strategy=" + strategy + ",filter=" + filter,
                                           learner,
                                           counters + mmlt.size());
            }
        }
    }

    @Override
    protected List<MMLTLearningExample<?, ?>> getAdditionalLearningExamples() {
        var modelFiles = listModelFiles();
        var result = new ArrayList<MMLTLearningExample<?, ?>>(modelFiles.size());

        for (String modelFile : modelFiles) {
            result.add(new Example(modelFile));
        }

        return result;
    }

    private static <S> int countTimers(MMLT<S, ?, ?, ?> mmlt) {
        int cntr = 0;

        for (S s : mmlt) {
            cntr += mmlt.getSortedTimers(s).size();
        }

        return cntr;
    }

    private static List<String> listModelFiles() {
        var models = new ArrayList<String>();
        try {
            var modelFiles = ExtensibleLStarMMLTIT.class.getResource("/mmlt");
            if (modelFiles != null) {
                try (Stream<Path> paths = Files.list(Paths.get(modelFiles.toURI()))) {
                    paths.filter(p -> p.toString().endsWith(".dot"))
                         .map(p -> p.getFileName().toString())
                         .forEach(models::add);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to list model files", e);
        }
        return models;
    }

    public static class Example implements MMLTLearningExample<String, String> {

        private final String name;
        private final MMLT<?, String, ?, String> mmlt;
        private final MMLTModelParams<String> params;

        public Example(String name) {
            this(name, -1);
        }

        public Example(String name, int maxTimerQueryWaiting) {
            this.name = name;

            var silentOutput = "void";
            var outputCombiner = StringSymbolCombiner.getInstance();
            var parser = DOTParsers.mmlt(silentOutput, outputCombiner);

            try (InputStream is = ExtensibleLStarMMLTIT.class.getResourceAsStream("/mmlt/" + name)) {
                var model = parser.readModel(is);
                var automaton = model.model;

                long maxTimeoutDelay = MMLTs.getMaximumTimeoutDelay(automaton);
                long maxTimerQueryWaitingFinal = (maxTimerQueryWaiting > 0) ?
                        maxTimerQueryWaiting :
                        MMLTs.getMaximumInitialTimerValue(automaton) * 2;

                this.mmlt = automaton;
                this.params =
                        new MMLTModelParams<>(silentOutput, outputCombiner, maxTimeoutDelay, maxTimerQueryWaitingFinal);
            } catch (IOException | FormatException e) {
                throw new RuntimeException("Unable to load model " + name, e);
            }
        }

        @Override
        public MMLTModelParams<String> getParams() {
            return this.params;
        }

        @Override
        public MMLT<?, String, ?, String> getReferenceAutomaton() {
            return this.mmlt;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

}
