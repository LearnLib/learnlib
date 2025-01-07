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
package de.learnlib.testsupport.example;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.automaton.transducer.SubsequentialTransducer;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.word.Word;

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
 */
public class DefaultLearningExample<I, D, A extends UniversalAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, D>>
        implements LearningExample<I, A> {

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

    public static class DefaultMooreLearningExample<I, D>
            extends DefaultLearningExample<I, Word<D>, MooreMachine<?, I, ?, D>> implements MooreLearningExample<I, D> {

        public <A extends MooreMachine<?, I, ?, D> & InputAlphabetHolder<I>> DefaultMooreLearningExample(A automaton) {
            this(automaton.getInputAlphabet(), automaton);
        }

        public DefaultMooreLearningExample(Alphabet<I> alphabet, MooreMachine<?, I, ?, D> referenceAutomaton) {
            super(alphabet, referenceAutomaton);
        }
    }

    public static class DefaultSSTLearningExample<I, D>
            extends DefaultLearningExample<I, Word<D>, SubsequentialTransducer<?, I, ?, D>>
            implements SSTLearningExample<I, D> {

        public <A extends SubsequentialTransducer<?, I, ?, D> & InputAlphabetHolder<I>> DefaultSSTLearningExample(A automaton) {
            this(automaton.getInputAlphabet(), automaton);
        }

        public DefaultSSTLearningExample(Alphabet<I> alphabet, SubsequentialTransducer<?, I, ?, D> referenceAutomaton) {
            super(alphabet, referenceAutomaton);
        }
    }

    public static class DefaultOneSEVPALearningExample<I> implements OneSEVPALearningExample<I> {

        private final VPAlphabet<I> alphabet;
        private final OneSEVPA<?, I> referenceAutomaton;

        public DefaultOneSEVPALearningExample(VPAlphabet<I> alphabet, OneSEVPA<?, I> referenceAutomaton) {
            this.alphabet = alphabet;
            this.referenceAutomaton = referenceAutomaton;
        }

        @Override
        public OneSEVPA<?, I> getReferenceAutomaton() {
            return this.referenceAutomaton;
        }

        @Override
        public VPAlphabet<I> getAlphabet() {
            return this.alphabet;
        }
    }

    public static class DefaultSPALearningExample<I> implements SPALearningExample<I> {

        private final SPA<?, I> referenceAutomaton;

        public DefaultSPALearningExample(SPA<?, I> referenceAutomaton) {
            this.referenceAutomaton = referenceAutomaton;
        }

        @Override
        public SPA<?, I> getReferenceAutomaton() {
            return this.referenceAutomaton;
        }

        @Override
        public ProceduralInputAlphabet<I> getAlphabet() {
            return this.referenceAutomaton.getInputAlphabet();
        }
    }

    public static class DefaultSBALearningExample<I> implements SBALearningExample<I> {

        private final SBA<?, I> referenceAutomaton;

        public DefaultSBALearningExample(SBA<?, I> referenceAutomaton) {
            this.referenceAutomaton = referenceAutomaton;
        }

        @Override
        public SBA<?, I> getReferenceAutomaton() {
            return this.referenceAutomaton;
        }

        @Override
        public ProceduralInputAlphabet<I> getAlphabet() {
            return this.referenceAutomaton.getInputAlphabet();
        }
    }

    public static class DefaultSPMMLearningExample<I, O> implements SPMMLearningExample<I, O> {

        private final SPMM<?, I, ?, O> referenceAutomaton;

        public DefaultSPMMLearningExample(SPMM<?, I, ?, O> referenceAutomaton) {
            this.referenceAutomaton = referenceAutomaton;
        }

        @Override
        public SPMM<?, I, ?, O> getReferenceAutomaton() {
            return this.referenceAutomaton;
        }

        @Override
        public ProceduralInputAlphabet<I> getAlphabet() {
            return this.referenceAutomaton.getInputAlphabet();
        }
    }

}
