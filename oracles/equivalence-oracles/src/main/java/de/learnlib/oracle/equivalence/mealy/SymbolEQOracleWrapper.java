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
package de.learnlib.oracle.equivalence.mealy;

import java.util.Collection;
import java.util.Objects;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.DetSuffixOutputAutomaton;
import net.automatalib.words.Word;

public class SymbolEQOracleWrapper<A extends DetSuffixOutputAutomaton<?, I, ?, Word<O>>, I, O>
        implements EquivalenceOracle<A, I, O> {

    private final EquivalenceOracle<? super A, I, Word<O>> wordEqOracle;

    public SymbolEQOracleWrapper(EquivalenceOracle<? super A, I, Word<O>> wordEqOracle) {
        this.wordEqOracle = wordEqOracle;
    }

    @Override
    public DefaultQuery<I, O> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        DefaultQuery<I, Word<O>> wordCeQry = wordEqOracle.findCounterExample(hypothesis, inputs);
        if (wordCeQry == null) {
            return null;
        }

        Word<O> hypOut = hypothesis.computeSuffixOutput(wordCeQry.getPrefix(), wordCeQry.getSuffix());
        Word<O> ceOut = wordCeQry.getOutput();

        int len = hypOut.length();
        if (len != ceOut.length()) {
            throw new IllegalStateException(
                    "Output word length does not align with suffix length, truncating CE will not work");
        }

        for (int i = 0; i < len; i++) {
            O hypSym = hypOut.getSymbol(i), ceSym = ceOut.getSymbol(i);

            if (!Objects.equals(hypSym, ceSym)) {
                DefaultQuery<I, O> result =
                        new DefaultQuery<>(wordCeQry.getPrefix(), wordCeQry.getSuffix().prefix(i + 1));
                result.answer(ceSym);
                return result;
            }
        }

        throw new IllegalStateException("Counterexample returned by underlying EQ oracle was none");
    }

}
