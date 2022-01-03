/* Copyright (C) 2013-2022 TU Dortmund
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

import java.util.Objects;

import de.learnlib.api.oracle.OmegaMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.OmegaQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.lasso.DFALassoImpl;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Word;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;

/**
 * @author Jeroen Meijer
 */
public class DFALassoEmptinessOracleImplTest
        extends AbstractLassoEmptinessOracleImplTest<Lasso.DFALasso<Character>, Boolean> {

    @Mock
    private OmegaMembershipOracle.DFAOmegaMembershipOracle<Integer, Character> omo;

    @BeforeMethod
    public void setUp() {
        super.setUp();

        Mockito.doAnswer(invocation -> {
            final OmegaQuery<Character, Boolean> q = Objects.requireNonNull(invocation.getArgument(0));
            if (q.getLoop().equals(Word.fromSymbols('a'))) {
                q.answer(true, 1);
            } else {
                q.answer(false, -1);
            }
            return null;
        }).when(omo).processQuery(ArgumentMatchers.any());

        Mockito.when(omo.isSameState(Word.epsilon(), 0, Word.fromSymbols('a'), 0)).thenReturn(true);
    }

    @Override
    protected LassoEmptinessOracleImpl<Lasso.DFALasso<Character>, Integer, Character, Boolean> createLassoEmptinessOracleImpl() {
        return new DFALassoEmptinessOracleImpl<>(omo);
    }

    @Override
    protected Boolean createOutput() {
        return true;
    }

    @Override
    protected Lasso.DFALasso<Character> createAutomaton() {
        final DFA<?, Character> dfa = AutomatonBuilders.forDFA(new CompactDFA<>(ALPHABET)).
                from("q0").on('a').loop().withAccepting("q0").withInitial("q0").create();
        return new DFALassoImpl<>(dfa, ALPHABET, 3);
    }

    @Override
    protected DefaultQuery<Character, Boolean> createQuery() {
        return new DefaultQuery<>(Word.fromSymbols('a'), true);
    }
}