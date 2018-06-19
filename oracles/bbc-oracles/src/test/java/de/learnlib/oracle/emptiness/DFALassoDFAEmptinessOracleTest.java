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
package de.learnlib.oracle.emptiness;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.api.oracle.OmegaMembershipOracle.DFAOmegaMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.OmegaQuery;
import de.learnlib.oracle.AbstractBreadthFirstOracle;
import de.learnlib.oracle.emptiness.AbstractLassoAutomatonEmptinessOracle.DFALassoDFAEmptinessOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.modelchecking.DFALassoImpl;
import net.automatalib.modelchecking.Lasso.DFALasso;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Word;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;

/**
 * @author Jeroen Meijer
 */
public class DFALassoDFAEmptinessOracleTest extends AbstractLassoAutomatonEmptinessTest<DFALasso<?, String>, Boolean> {

    @Mock
    private DFAOmegaMembershipOracle<Integer, String> dfaOmegaMembershipOracle;

    @Mock
    private DFAOmegaMembershipOracle<Integer, Character> dfaOmegaMembershipOracle2;

    @BeforeMethod
    public void setUp() {
        super.setUp();
        Mockito.doAnswer(invocation -> {
            final OmegaQuery<Integer, String, Boolean> q = invocation.getArgument(0);
            if (q.getInput().equals(Word.fromSymbols("a", "a", "a"))) {
                q.answer(true);
                final List<Integer> states = new ArrayList<>();
                states.add(0);
                states.add(0);
                states.add(0);
                states.add(0);
                q.setStates(states);
            } else {
                q.answer(false);
            }
            return null;
        }).when(dfaOmegaMembershipOracle).processQuery(ArgumentMatchers.any());
        Mockito.when(dfaOmegaMembershipOracle.isSameState(ArgumentMatchers.any(),
                                                          ArgumentMatchers.any(),
                                                          ArgumentMatchers.any(),
                                                          ArgumentMatchers.any())).thenReturn(true);
    }

    @Override
    protected AbstractLassoAutomatonEmptinessOracle<DFALasso<?, String>, ?, String, ?> createLassoAutomatonEmptinessOracle() {
        return new DFALassoDFAEmptinessOracle<>(dfaOmegaMembershipOracle);
    }

    @Override
    protected DFALassoImpl<?, String> createLasso() {
        final DFA<?, String> dfa = AutomatonBuilders.forDFA(new CompactDFA<>(ALPHABET)).
                from("q0").on("a").loop().withAccepting("q0").withInitial("q0").create();
        return new DFALassoImpl<>(dfa, ALPHABET, 3);
    }

    @Override
    protected DefaultQuery<String, ?> createQuery() {
        return new DefaultQuery<>(Word.fromSymbols("a", "a", "a"), true);
    }

    @Override
    protected AbstractBreadthFirstOracle<? extends SimpleDTS<?, Character>, Character, Boolean, OmegaQuery<Integer, Character, Boolean>> createBreadthFirstOracle(
            int maxWords) {
        return new DFALassoDFAEmptinessOracle<>(dfaOmegaMembershipOracle2);
    }
}
