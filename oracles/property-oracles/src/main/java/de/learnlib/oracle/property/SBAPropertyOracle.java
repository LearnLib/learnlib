/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.oracle.property;

import java.util.Collection;
import java.util.function.Function;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.PropertyOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.modelchecking.ModelChecker;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link PropertyOracle} for verifying properties on {@link SBA}s. Currently only works with model checkers whose
 * violations can be extracted to a single {@link Word}-based representative (which are likely violated safety
 * properties.
 *
 * @param <I>
 *         input symbol type
 * @param <P>
 *         property type
 * @param <R>
 *         counterexample type
 */
public class SBAPropertyOracle<I, P, R> implements PropertyOracle<I, SBA<?, I>, P, Boolean> {

    private final P property;
    private final MembershipOracle<I, Boolean> membershipOracle;
    private final ModelChecker<I, SBA<?, I>, P, R> modelChecker;
    private final Function<R, Word<I>> extractor;

    private DefaultQuery<I, Boolean> counterexample;

    public SBAPropertyOracle(P property,
                             MembershipOracle<I, Boolean> membershipOracle,
                             ModelChecker<I, SBA<?, I>, P, R> modelChecker,
                             Function<R, Word<I>> extractor) {
        this.property = property;
        this.membershipOracle = membershipOracle;
        this.modelChecker = modelChecker;
        this.extractor = extractor;
    }

    @Override
    public P getProperty() {
        return property;
    }

    @Override
    public @Nullable DefaultQuery<I, Boolean> getCounterExample() {
        return counterexample;
    }

    @Override
    public @Nullable DefaultQuery<I, Boolean> disprove(SBA<?, I> hypothesis, Collection<? extends I> inputs) {
        final R ce = modelChecker.findCounterExample(hypothesis, inputs, property);

        if (ce != null) {
            final Word<I> witness = extractor.apply(ce);
            if (this.membershipOracle.answerQuery(witness)) {
                this.counterexample = new DefaultQuery<>(witness, true);
            }
        }

        return this.counterexample;
    }

    @Override
    public @Nullable DefaultQuery<I, Boolean> doFindCounterExample(SBA<?, I> hypothesis,
                                                                   Collection<? extends I> inputs) {
        final R ce = modelChecker.findCounterExample(hypothesis, inputs, property);

        if (ce != null) {
            final Word<I> witness = extractor.apply(ce);
            final boolean sul = membershipOracle.answerQuery(witness);
            final boolean hyp = hypothesis.accepts(witness);
            if (sul != hyp) {
                return new DefaultQuery<>(witness, sul);
            }
        }

        return null;
    }
}
