/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.algorithm.lstar.dfa;

import java.util.Collections;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithm.lstar.closing.ClosingStrategies;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

/**
 * Implementation of the L* algorithm by Dana Angluin.
 *
 * @param <I>
 *         input symbol class.
 */
public class ClassicLStarDFA<I> extends ExtensibleLStarDFA<I> {

    @GenerateBuilder
    public ClassicLStarDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
        super(alphabet,
              oracle,
              Collections.singletonList(Word.epsilon()),
              ObservationTableCEXHandlers.CLASSIC_LSTAR,
              ClosingStrategies.CLOSE_FIRST);
    }
}
