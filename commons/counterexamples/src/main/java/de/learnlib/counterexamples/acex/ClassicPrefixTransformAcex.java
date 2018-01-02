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
package de.learnlib.counterexamples.acex;

import java.util.Objects;
import java.util.function.Function;

import de.learnlib.acex.impl.AbstractBaseCounterexample;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;

/**
 * An abstract counterexample based on prefix transformations. Unlike {@link OutInconsPrefixTransformAcex} and its
 * derivatives, the effect of this abstract counterexamples is not directly determined by the system outputs, but
 * instead by whether they match the hypothesis output or not (as described in the paper <a
 * href="jmlr.org/proceedings/papers/v34/isberner14a.pdf"> <i>An Abstract Framework for Counterexample Analysis in
 * Active Automata Learning</i> (M. Isberner, B. Steffen; Proc. ICGI 2014)</a>.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Malte Isberner
 */
public class ClassicPrefixTransformAcex<I, D> extends AbstractBaseCounterexample<Boolean> {

    private final Word<I> suffix;
    private final MembershipOracle<I, D> oracle;
    private final SuffixOutput<I, D> hypOut;

    private final Function<Word<I>, Word<I>> asTransform;

    public ClassicPrefixTransformAcex(Word<I> suffix,
                                      MembershipOracle<I, D> oracle,
                                      SuffixOutput<I, D> hypOut,
                                      Function<Word<I>, Word<I>> asTransform) {
        this(suffix, suffix.length() + 1, oracle, hypOut, asTransform);
    }

    protected ClassicPrefixTransformAcex(Word<I> suffix,
                                         int length,
                                         MembershipOracle<I, D> oracle,
                                         SuffixOutput<I, D> hypOut,
                                         Function<Word<I>, Word<I>> asTransform) {
        super(length);
        this.suffix = suffix;
        this.oracle = oracle;
        this.asTransform = asTransform;
        this.hypOut = hypOut;
    }

    @Override
    protected Boolean computeEffect(int index) {
        Word<I> prefix = this.suffix.prefix(index);
        Word<I> suffix = this.suffix.subWord(index);

        Word<I> asPrefix = asTransform.apply(prefix);

        return Objects.equals(hypOut.computeSuffixOutput(asPrefix, suffix), oracle.answerQuery(asPrefix, suffix));
    }

    @Override
    public boolean checkEffects(Boolean eff1, Boolean eff2) {
        return Objects.equals(eff1, eff2);
    }

}
