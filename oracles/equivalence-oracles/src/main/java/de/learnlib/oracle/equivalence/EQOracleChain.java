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
package de.learnlib.oracle.equivalence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

public class EQOracleChain<A, I, D> implements EquivalenceOracle<A, I, D> {

    private final List<EquivalenceOracle<? super A, I, D>> oracles;

    @SafeVarargs
    public EQOracleChain(EquivalenceOracle<? super A, I, D>... oracles) {
        this(Arrays.asList(oracles));
    }

    public EQOracleChain(List<? extends EquivalenceOracle<? super A, I, D>> oracles) {
        this.oracles = new ArrayList<>(oracles);
    }

    @Override
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        for (EquivalenceOracle<? super A, I, D> eqOracle : oracles) {
            DefaultQuery<I, D> ceQry = eqOracle.findCounterExample(hypothesis, inputs);
            if (ceQry != null) {
                return ceQry;
            }
        }
        return null;
    }

    public static class DFAEQOracleChain<I> extends EQOracleChain<DFA<?, I>, I, Boolean>
            implements DFAEquivalenceOracle<I> {

        @SafeVarargs
        public DFAEQOracleChain(EquivalenceOracle<? super DFA<?, I>, I, Boolean>... oracles) {
            super(oracles);
        }

        public DFAEQOracleChain(List<? extends EquivalenceOracle<? super DFA<?, I>, I, Boolean>> oracles) {
            super(oracles);
        }
    }

    public static class MealyEQOracleChain<I, O> extends EQOracleChain<MealyMachine<?, I, ?, O>, I, Word<O>>
            implements MealyEquivalenceOracle<I, O> {

        @SafeVarargs
        public MealyEQOracleChain(EquivalenceOracle<? super MealyMachine<?, I, ?, O>, I, Word<O>>... oracles) {
            super(oracles);
        }

        public MealyEQOracleChain(List<? extends EquivalenceOracle<? super MealyMachine<?, I, ?, O>, I, Word<O>>> oracles) {
            super(oracles);
        }
    }

}
