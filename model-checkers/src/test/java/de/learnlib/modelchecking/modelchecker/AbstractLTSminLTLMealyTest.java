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
import java.util.HashSet;

import de.learnlib.api.modelchecking.counterexample.Lasso.MealyLasso;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public abstract class AbstractLTSminLTLMealyTest<M extends AbstractLTSminLTLMealy<String, String>>
        extends AbstractLTSminLTLTest<MealyMachine<?, String, ?, String>, M, MealyLasso<?, String, String>> {

    @SuppressWarnings("unchecked")
    @Test
    public void testSkipOutputs() throws Exception {
        final HashSet<String> skip = new HashSet<>();
        skip.add("1");
        getModelChecker().setSkipOutputs(skip);

        final Alphabet<String> alphabet = Alphabets.fromArray("a");
        final MealyMachine<?, String, ?, String> mealy = AutomatonBuilders.forMealy(
                new CompactMealy<String, String>(alphabet)
        ).from("q0").on("a").withOutput("1").loop().withInitial("q0").create();

        AbstractLTSminLTLMealy<String, String> spy = Mockito.spy(getModelChecker());

        File etf = File.createTempFile("tmp", ".etf");
        etf.deleteOnExit();

        spy.automaton2ETF(mealy, alphabet, etf);

        // test the transition is removed from the Mealy machine
        ArgumentCaptor<MealyMachine<?, String, ?, String>> modifiedMealy = ArgumentCaptor.forClass(MealyMachine.class);
        Mockito.verify(spy).mealy2ETF(modifiedMealy.capture(), Mockito.eq(alphabet), Mockito.eq(etf));
        Assert.assertEquals(modifiedMealy.getValue().computeOutput(Word.fromSymbols("a")), Word.epsilon());
        if (!etf.delete()) {
            throw new Exception();
        }
    }
}