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
package de.learnlib.examples;

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * A {@link LearningExample learning example} that directly stores the alphabet and the reference automaton in its
 * fields.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output type
 * @param <A>
 *         automaton type
 *
 * @author Malte Isberner
 */
public class DefaultLearningExample<I, D, A extends UniversalAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, D>>
        implements LearningExample<I, D, A> {

    private final Alphabet<I> alphabet;
    private final A referenceAutomaton;

    public DefaultLearningExample(Alphabet<I> alphabet, A referenceAutomaton) {
        this.alphabet = alphabet;
        this.referenceAutomaton = referenceAutomaton;
    }

    @Override
    public A getReferenceAutomaton() {
        return referenceAutomaton;
    }

    @Override
    public Alphabet<I> getAlphabet() {
        return alphabet;
    }

    public static class DefaultDFALearningExample<I> extends DefaultLearningExample<I, Boolean, DFA<?, I>>
            implements DFALearningExample<I> {

        public <A extends DFA<?, I> & InputAlphabetHolder<I>> DefaultDFALearningExample(A automaton) {
            this(automaton.getInputAlphabet(), automaton);
        }

        public DefaultDFALearningExample(Alphabet<I> alphabet, DFA<?, I> referenceAutomaton) {
            super(alphabet, referenceAutomaton);
        }
    }

    public static class DefaultMealyLearningExample<I, D>
            extends DefaultLearningExample<I, Word<D>, MealyMachine<?, I, ?, D>> implements MealyLearningExample<I, D> {

        public <A extends MealyMachine<?, I, ?, D> & InputAlphabetHolder<I>> DefaultMealyLearningExample(A automaton) {
            this(automaton.getInputAlphabet(), automaton);
        }

        public DefaultMealyLearningExample(Alphabet<I> alphabet, MealyMachine<?, I, ?, D> referenceAutomaton) {
            super(alphabet, referenceAutomaton);
        }
    }

}
