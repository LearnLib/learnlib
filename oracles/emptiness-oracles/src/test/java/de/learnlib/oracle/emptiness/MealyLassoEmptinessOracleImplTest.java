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

import de.learnlib.api.oracle.OmegaMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.OmegaQuery;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.lasso.MealyLassoImpl;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Word;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;

/**
 * @author Jeroen Meijer
 */
public class MealyLassoEmptinessOracleImplTest
        extends AbstractLassoEmptinessOracleImplTest<Lasso.MealyLasso<Character, Character>, Word<Character>> {

    @Mock
    private OmegaMembershipOracle.MealyOmegaMembershipOracle<Integer, Character, Character> omo;

    @BeforeMethod
    public void setUp() {
        super.setUp();

        Mockito.doAnswer(invocation -> {
            final OmegaQuery<Character, Word<Character>> q = invocation.getArgument(0);
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