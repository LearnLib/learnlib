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

import java.util.Collection;

import de.learnlib.query.DefaultQuery;
import net.automatalib.word.Word;

/**
 * Default implementation for a passive learning example.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @see DefaultLearningExample
 */
public class DefaultPassiveLearningExample<I, D> implements PassiveLearningExample<I, D> {

    private final Collection<DefaultQuery<I, D>> samples;

    public DefaultPassiveLearningExample(Collection<DefaultQuery<I, D>> samples) {
        this.samples = samples;
    }

    @Override
    public Collection<DefaultQuery<I, D>> getSamples() {
        return this.samples;
    }

    public static class DefaultDFAPassiveLearningExample<I> extends DefaultPassiveLearningExample<I, Boolean>
            implements DFAPassiveLearningExample<I> {

        public DefaultDFAPassiveLearningExample(Collection<DefaultQuery<I, Boolean>> samples) {
            super(samples);
        }
    }

    public static class DefaultMealyPassiveLearningExample<I, O> extends DefaultPassiveLearningExample<I, Word<O>>
            implements MealyPassiveLearningExample<I, O> {

        public DefaultMealyPassiveLearningExample(Collection<DefaultQuery<I, Word<O>>> samples) {
            super(samples);
        }
    }

    public static class DefaultMoorePassiveLearningExample<I, O> extends DefaultPassiveLearningExample<I, Word<O>>
            implements MoorePassiveLearningExample<I, O> {

        public DefaultMoorePassiveLearningExample(Collection<DefaultQuery<I, Word<O>>> samples) {
            super(samples);
        }
    }

    public static class DefaultSSTPassiveLearningExample<I, O> extends DefaultPassiveLearningExample<I, Word<O>>
            implements SSTPassiveLearningExample<I, O> {

        public DefaultSSTPassiveLearningExample(Collection<DefaultQuery<I, Word<O>>> samples) {
            super(samples);
        }
    }
}
