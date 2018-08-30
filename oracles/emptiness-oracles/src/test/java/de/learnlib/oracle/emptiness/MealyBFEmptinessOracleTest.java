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

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Word;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;

/**
 * @author Jeroen Meijer
 */
public class MealyBFEmptinessOracleTest
        extends AbstractBFEmptinessOracleTest<MealyMachine<?, Character, ?, Character>, Word<Character>> {

    @Mock
    private MembershipOracle.MealyMembershipOracle<Character, Character> mo;

    @Override
    protected AbstractBFEmptinessOracle<MealyMachine<?, Character, ?, Character>, Character, Word<Character>>
            createBreadthFirstEmptinessOracle() {
        return new MealyBFEmptinessOracle<>(mo, MULTIPLIER);
    }

    @Override
    protected MealyMachine<?, Character, ?, Character> createAutomaton() {
        return AutomatonBuilders.forMealy(new CompactMealy<Character, Character>(ALPHABET)).
                        from("q0").on('a').withOutput('1').loop().withInitial("q0").create();
    }

    @Override
    protected DefaultQuery<Character, Word<Character>> createQuery() {
        return new DefaultQuery<>(Word.epsilon(), Word.fromSymbols('a'), Word.fromSymbols('1'));
    }

    @BeforeMethod
    public void setUp() {
        super.setUp();
        Mockito.doAnswer(invocation -> {
            final DefaultQuery<Character, Word<Character>> q = invocation.getArgument(0);
            if (q.getInput().equals(Word.fromSymbols('a'))) {
                q.answer(Word.fromSymbols('1'));
            } else {
                q.answer(null);
            }
            return null;
        }).when(mo).processQuery(ArgumentMatchers.any());
    }
}