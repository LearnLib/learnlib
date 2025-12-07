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
package de.learnlib.algorithm.lstar.mmlt.cex;

import java.util.function.Function;

import de.learnlib.acex.AbstractBaseCounterexample;
import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLT;
import de.learnlib.oracle.TimedQueryOracle;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract counterexample used by the {@link ExtensibleLStarMMLT} learner.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class MMLTInconsPrefixTransformAcex<I, O> extends AbstractBaseCounterexample<Word<TimedOutput<O>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MMLTInconsPrefixTransformAcex.class);

    private final TimedQueryOracle<I, O> timeOracle;
    private final Word<TimedInput<I>> suffix;

    private final Function<Word<TimedInput<I>>, Word<TimedInput<I>>> asTransform;

    /**
     * Constructor.
     *
     * @param suffix
     *         the suffix of the counterexample, i.e., the word that we analyze
     * @param timeOracle
     *         the membership oracle
     * @param asTransform
     *         a mapping that transforms an access sequence of a system state to its canonical access sequence
     */
    public MMLTInconsPrefixTransformAcex(Word<TimedInput<I>> suffix,
                                         TimedQueryOracle<I, O> timeOracle,
                                         Function<Word<TimedInput<I>>, Word<TimedInput<I>>> asTransform) {
        super(suffix.length());
        this.timeOracle = timeOracle;
        this.suffix = suffix;
        this.asTransform = asTransform;
    }

    @Override
    public Word<TimedOutput<O>> computeEffect(int index) {
        // Split the word at our index:
        Word<TimedInput<I>> prefix = this.suffix.prefix(index); // everything up to *index* (exclusive)
        Word<TimedInput<I>> suffix = this.suffix.subWord(index); // everything from *index* (inclusive)

        // Identify access sequence of system state for prefix:
        Word<TimedInput<I>> accessSequence = this.asTransform.apply(prefix);

        // Query *hypothesis state* + *suffix*:
        return this.timeOracle.answerQuery(accessSequence, suffix);
    }

    @Override
    public boolean checkEffects(Word<TimedOutput<O>> eff1, Word<TimedOutput<O>> eff2) {
        // Same behavior at different indices?
        LOGGER.debug("Comparing ({}) AND ({}): {}", eff1, eff2, eff2.isSuffixOf(eff1));
        return eff2.isSuffixOf(eff1);
    }
}
