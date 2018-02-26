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
import de.learnlib.api.exception.ModelCheckingException;
import de.learnlib.api.modelchecking.counterexample.Lasso.DFALasso;
import de.learnlib.api.modelchecking.modelchecker.ModelChecker.DFAModelCheckerLasso;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.etf.writer.DFA2ETFWriter;
import net.automatalib.serialization.fsm.parser.FSM2DFAParser;
import net.automatalib.serialization.fsm.parser.FSMParseException;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * An LTL model checker using LTSmin for DFAs.
 *
 * An important feature of this {@link de.learnlib.api.modelchecking.modelchecker.ModelChecker.DFAModelChecker}, is
 * that it will check if a given DFA hypothesis is prefix-closed.
 *
 * Another important feature is that rejecting states are NOT part of the LTS. This avoids the need for an
 * unconditional fairness constraint in LTL formulae.
 *
 * @author Jeroen Meijer
 *
 * @see DFAs#isPrefixClosed(DFA, Alphabet)
 *
 * @param <I> the input type
 */
public class LTSminLTLDFA<I>
        extends AbstractLTSminLTL<I, DFA<?, I>, DFALasso<?, I>>
        implements DFAModelCheckerLasso<I, String> {

    /**
     * The index in the FSM state vector for accept/reject.
     */
    public static final String LABEL_NAME = "label";

    /**
     * The value in the state vector for acceptance.
     */
    public static final String LABEL_VALUE = "accept";

    @GenerateBuilder(defaults = BuilderDefaults.class)
    public LTSminLTLDFA(boolean keepFiles, Function<String, I> string2Input, int minimumUnfolds, double multiplier) {
        super(keepFiles, string2Input, minimumUnfolds, multiplier);
    }


    @Override
    protected void automaton2ETF(DFA<?, I> automaton, Collection<? extends I> inputs, File etf) throws IOException {
        dfa2ETF(automaton, inputs, etf);
    }

    /**
     * Writes the given {@code dfa} to {@code etf}, while skipping rejecting states.
     *
     * @param dfa the DFA to write.
     * @param inputs the alphabet.
     * @param etf the file to write to.
     *
     * @param <S> the state type
     *
     * @throws IOException see {@link DFA2ETFWriter#write(File, DFA, Alphabet)}.
     */
    private <S> void dfa2ETF(DFA<S, I> dfa, Collection<? extends I> inputs, File etf) throws IOException {
        // check that the DFA rejects the empty language
        if (DFAs.acceptsEmptyLanguage(dfa)) {
            throw new ModelCheckingException("DFA accepts the empty language, the LTS for such a DFA is not defined.");
        }

        final Alphabet<I> alphabet = Alphabets.fromCollection(inputs);

        // check the DFA is prefix-closed
        if (!DFAs.isPrefixClosed(dfa, alphabet)) {
            throw new ModelCheckingException("DFA is not prefix closed.");
        }

        // remove all rejecting states
        final MutableDFA<?, I> copy = new CompactDFA<>(alphabet, dfa.size());
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, dfa, inputs, copy, dfa::isAccepting, (s, i, t) -> true);
        DFA2ETFWriter.write(etf, copy, alphabet);
    }

    /**
     * Converts the FSM file to a {@link DFALasso}.
     *
     * @param hypothesis the DFA used to compute the number of loop unrolls.
     *
     * @see AbstractLTSminLTL#fsm2Lasso(File, SimpleDTS)
     */
    @Override
    protected DFALasso<?, I> fsm2Lasso(File fsm, DFA<?, I> hypothesis) throws IOException, FSMParseException {
        CompactDFA<I> dfa = FSM2DFAParser.parse(fsm, getString2Input(), LABEL_NAME, LABEL_VALUE);
        //dfa = DFAs.complete(dfa, dfa.getInputAlphabet());
        //dfa = HopcroftMinimization.minimizeDFA(dfa, dfa.getInputAlphabet());

        return new DFALasso<>(dfa, dfa.getInputAlphabet(), computeUnfolds(hypothesis.size()));
    }
}
