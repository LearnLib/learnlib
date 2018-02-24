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
package de.learnlib.modelchecking.modelchecker;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.serialization.etf.writer.Mealy2ETFWriterAlternating;
import net.automatalib.serialization.fsm.parser.FSM2MealyParserAlternating;
import net.automatalib.serialization.fsm.parser.FSMParseException;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * An LTL model checker using LTSmin for Mealy machines using alternating edge semantics.
 *
 * The implementation uses {@link FSM2MealyParserAlternating}, and {@link Mealy2ETFWriterAlternating}, to read the
 * {@link de.learnlib.api.modelchecking.counterexample.Lasso.MealyLasso}, and write the {@link MealyMachine}
 * respectively.
 *
 * @author Jeroen Meijer
 *
 * @param <I> the input type
 * @param <O> the output type
 */
public class LTSminLTLAlternating<I, O> extends AbstractLTSminLTLMealy<I, O> {

    @GenerateBuilder(defaults = BuilderDefaults.class)
    public LTSminLTLAlternating(boolean keepFiles,
                                Function<String, I> string2Input,
                                Function<String, O> string2Output,
                                int minimumUnfolds,
                                double multiplier,
                                boolean inheritIO,
                                Collection<? super O> skipOutputs) {
        super(keepFiles, string2Input, string2Output, minimumUnfolds, multiplier, inheritIO, skipOutputs);
    }

    @Override
    protected CompactMealy<I, O> fsm2Mealy(File fsm) throws IOException, FSMParseException {
        return FSM2MealyParserAlternating.parse(fsm, getString2Input(), getString2Output());
    }

    @Override
    protected void mealy2ETF(MealyMachine<?, I, ?, O> automaton, Collection<? extends I> inputs, File etf)
            throws IOException {
        final Alphabet<I> alphabet = Alphabets.fromCollection(inputs);
        Mealy2ETFWriterAlternating.write(etf, automaton, alphabet);
    }
}
