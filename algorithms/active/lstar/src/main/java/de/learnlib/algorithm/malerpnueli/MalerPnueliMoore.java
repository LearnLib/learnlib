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
package de.learnlib.algorithm.malerpnueli;

import java.util.Collections;
import java.util.List;

import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithm.lstar.closing.ClosingStrategies;
import de.learnlib.algorithm.lstar.closing.ClosingStrategy;
import de.learnlib.algorithm.lstar.moore.ExtensibleLStarMoore;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;

/**
 * A {@link MooreMachine}-based specialization of the L* learner which uses the counterexample analysis strategy
 * proposed by Maler &amp; Pnueli.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class MalerPnueliMoore<I, O> extends ExtensibleLStarMoore<I, O> {

    public MalerPnueliMoore(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
        this(alphabet, oracle, Collections.emptyList(), ClosingStrategies.CLOSE_FIRST);
    }

    @GenerateBuilder(defaults = BuilderDefaults.class)
    public MalerPnueliMoore(Alphabet<I> alphabet,
                            MembershipOracle<I, Word<O>> oracle,
                            List<Word<I>> initialSuffixes,
                            ClosingStrategy<? super I, ? super Word<O>> closingStrategy) {
        super(alphabet, oracle, initialSuffixes, ObservationTableCEXHandlers.MALER_PNUELI, closingStrategy);
    }

}
