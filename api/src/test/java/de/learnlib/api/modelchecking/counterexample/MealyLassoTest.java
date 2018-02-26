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
package de.learnlib.api.modelchecking.counterexample;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.learnlib.api.modelchecking.counterexample.Lasso.MealyLasso;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public class MealyLassoTest extends AbstractLassoTest<MealyLasso<?, String, String>> {

    @Override
    protected MealyLasso<?, String, String> getLasso(Word<String> prefix, Word<String> loop, int unfoldTimes) {
        return new MealyLasso<>(new MealyMachineMock(prefix, loop), getAlphabet(), 1);
    }

    @Test
    public void testGetOutput() {
        final MealyLasso<?, String, String> lasso = getLasso(Word.epsilon(), Word.fromSymbols("a"), 1);
        Assert.assertEquals(lasso.getOutput(), Word.fromSymbols(MealyMachineMock.OUTPUT));
    }

    private class MealyMachineMock implements MealyMachine<Integer, String, CompactMealyTransition<String>, String> {

        public static final String OUTPUT = "test";

        private final Word<String> prefix;
        private final Word<String> word;

        MealyMachineMock(Word<String> prefix, Word<String> loop) {
            this.prefix = prefix;
            word = prefix.concat(loop);
        }

        @Nullable
        @Override
        public String getTransitionOutput(CompactMealyTransition<String> transition) {
            return OUTPUT;
        }

        @Nonnull
        @Override
        public Collection<Integer> getStates() {
            return CollectionsUtil.intRange(0, word.length());
        }

        @Nullable
        @Override
        public CompactMealyTransition<String> getTransition(Integer state, @Nullable String input) {
            final CompactMealyTransition<String> result;
            if (word.getSymbol(state).equals(input)) {
                if (state < word.length() - 1) {
                    result = new CompactMealyTransition<>(state + 1, OUTPUT);
                } else {
                    result = new CompactMealyTransition<>(prefix.length(), OUTPUT);
                }
            } else {
                result = null;
            }

            return result;
        }

        @Nonnull
        @Override
        public Integer getSuccessor(CompactMealyTransition<String> transition) {
            return transition.getSuccId();
        }

        @Nullable
        @Override
        public Integer getInitialState() {
            return 0;
        }
    }
}
