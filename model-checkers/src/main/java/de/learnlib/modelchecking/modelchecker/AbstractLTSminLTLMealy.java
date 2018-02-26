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
import java.util.Collections;
import java.util.function.Function;

import de.learnlib.api.modelchecking.counterexample.Lasso.MealyLasso;
import de.learnlib.api.modelchecking.modelchecker.ModelChecker.MealyModelChecker;
import de.learnlib.api.modelchecking.modelchecker.ModelChecker.MealyModelCheckerLasso;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.serialization.fsm.parser.FSMParseException;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.util.automata.transout.MealyFilter;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * An LTL model checker using LTSmin for Mealy machines.
 *
 * A feature of this {@link de.learnlib.api.modelchecking.modelchecker.ModelChecker}, is that one can remove
 * particular output symbols from the a given MealyMachine hypothesis. This is useful when those symbols are actually
 * symbols representing system deadlocks. When checking LTL formulae special attention has to be given to deadlock
 * situations.
 *
 * @author Jeroen Meijer
 *
 * @param <I> the input type.
 * @param <O> the output type.
 */
public abstract class AbstractLTSminLTLMealy<I, O>
        extends AbstractLTSminLTL<I, MealyMachine<?, I, ?, O>, MealyLasso<?, I, O>>
        implements MealyModelChecker<I, O, String, MealyLasso<?, I, O>>, MealyModelCheckerLasso<I, O, String> {

    /**
     * @see #getString2Output()
     */
    private final Function<String, O> string2Output;

    /**
     * @see #getSkipOutputs()
     * @see #setSkipOutputs(Collection)
     */
    private Collection<? super O> skipOutputs;

    /**
     * Constructs a new AbstractLTSminLTLMealy.
     *
     * @param string2Output the function that transforms edges in the FSM file to actual output.
     * @param skipOutputs the set of outputs that need to be skipped while writing the Mealy machine to ETF.
     *
     * @see AbstractLTSminLTL
     */
    protected AbstractLTSminLTLMealy(boolean keepFiles,
                                     Function<String, I> string2Input,
                                     Function<String, O> string2Output,
                                     int minimumUnfolds,
                                     double multiplier,
                                     Collection<? super O> skipOutputs) {
        super(keepFiles, string2Input, minimumUnfolds, multiplier);
        this.string2Output = string2Output;
        this.skipOutputs = skipOutputs == null ? Collections.emptyList() : skipOutputs;
    }

    /**
     * Gets a function that transforms edges in the FSM file to actual output.
     *
     * @return the Function.
     */
    public Function<String, O> getString2Output() {
        return string2Output;
    }

    /**
     * Gets a set of outputs that need to be skipped while writing the Mealy machine to ETF.
     *
     * @return the Colleciton.
     */
    public Collection<? super O> getSkipOutputs() {
        return skipOutputs;
    }

    /**
     * Sets a set of outputs that need to be skipped while writing the Mealy machine to ETF.
     */
    public void setSkipOutputs(Collection<? super O> skipOutputs) {
        this.skipOutputs = skipOutputs;
    }

    /**
     * Converts the given {@code fsm} to a {@link CompactMealy}.
     *
     * @param fsm the FSM to convert.
     *
     * @return the {@link CompactMealy}.
     *
     * @throws IOException when {@code fsm} can not be read.
     * @throws FSMParseException when {@code fsm} is invalid.
     */
    protected abstract CompactMealy<I, O> fsm2Mealy(File fsm) throws IOException, FSMParseException;

    /**
     * Writes the {@link MealyMachine} to the {@code etf} file while pruning way the outputs given in
     * {@link #getSkipOutputs()}.
     *
     * @param mealyMachine the {@link MealyMachine} to write.
     *
     * @see AbstractLTSminLTL#automaton2ETF(SimpleDTS, Collection, File)
     *
     * @throws IOException see {@link #mealy2ETF(MealyMachine, Collection, File)}.
     */
    @Override
    protected final void automaton2ETF(MealyMachine<?, I, ?, O> mealyMachine, Collection<? extends I> inputs, File etf)
            throws IOException {
        final Alphabet<I> alphabet = Alphabets.fromCollection(inputs);
        mealy2ETF(MealyFilter.pruneTransitionsWithOutput(mealyMachine, alphabet, skipOutputs), inputs, etf);
    }

    /**
     * Writes the given {@link MealyMachine} to the {@code etf} file.
     *
     * @param automaton the {@link MealyMachine} to write.
     * @param inputs the alphabet.
     * @param etf the file to write to.
     *
     * @throws IOException when {@code etf} can not be read.
     */
    protected abstract void mealy2ETF(MealyMachine<?, I, ?, O> automaton, Collection<? extends I> inputs, File etf)
            throws IOException;

    /**
     * Converts the FSM to a Lasso.
     *
     * @param fsm the FSM to read.
     * @param hypothesis the hypothesis used to compute the number of loop unfolds.
     *
     * @return the {@link MealyLasso}.
     *
     * @throws IOException see {@link #fsm2Mealy(File)}.
     * @throws FSMParseException see {@link #fsm2Mealy(File)}.
     */
    @Override
    protected final MealyLasso<?, I, O> fsm2Lasso(File fsm, MealyMachine<?, I, ?, O> hypothesis)
            throws IOException, FSMParseException {
        CompactMealy<I, O> mealy = fsm2Mealy(fsm);
        // miniminzation is generally not possible, since a Lasso is partial.
        // mealy = HopcroftMinimization.minimizeMealy(mealy);

        return new MealyLasso<>(mealy, mealy.getInputAlphabet(), computeUnfolds(hypothesis.size()));
    }
}
