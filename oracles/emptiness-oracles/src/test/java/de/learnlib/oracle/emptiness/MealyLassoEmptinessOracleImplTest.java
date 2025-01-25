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
package de.learnlib.oracle.emptiness;

import java.util.Objects;

import de.learnlib.oracle.OmegaMembershipOracle.MealyOmegaMembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.OmegaQuery;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.impl.MealyLassoImpl;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.word.Word;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;

public class MealyLassoEmptinessOracleImplTest
        extends AbstractLassoEmptinessOracleImplTest<Lasso.MealyLasso<Character, Character>, Word<Character>> {

    @Mock
    private MealyOmegaMembershipOracle<Integer, Character, Character> omo;

    @BeforeMethod
    public void setUp() {
        super.setUp();

        Mockito.doAnswer(invocation -> {
            final OmegaQuery<Character, Word<Character>> q = Objects.requireNonNull(invocation.getArgument(0));
            if (q.getLoop().equals(Word.fromSymbols('a'))) {
                q.answer(Word.fromSymbols('1'), 1);
            } else {
                q.answer(Word.epsilon(), -1);
            }
            return null;
        }).when(omo).processQuery(ArgumentMatchers.any());

        Mockito.when(omo.isSameState(Word.epsilon(), 0, Word.fromSymbols('a'), 0)).thenReturn(true);
    }

    @Override
    protected LassoEmptinessOracleImpl<Lasso.MealyLasso<Character, Character>, Integer, Character, Word<Character>> createLassoEmptinessOracleImpl() {
        return new MealyLassoEmptinessOracleImpl<>(omo);
    }

    @Override
    protected Word<Character> createOutput() {
        return Word.fromSymbols('1');
    }

    @Override
    protected Lasso.MealyLasso<Character, Character> createAutomaton() {
        final MealyMachine<?, Character, ?, Character> mealy =
                AutomatonBuilders.forMealy(new CompactMealy<Character, Character>(ALPHABET)).
                        from("q0").on('a').withOutput('1').loop().withInitial("q0").create();
        return new MealyLassoImpl<>(mealy, ALPHABET, 3);
    }

    @Override
    protected DefaultQuery<Character, Word<Character>> createQuery() {
        return new DefaultQuery<>(Word.epsilon(), Word.fromSymbols('a'), Word.fromSymbols('1'));
    }
}
