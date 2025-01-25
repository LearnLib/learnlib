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
package de.learnlib.acex;

import java.util.function.Function;

import de.learnlib.oracle.MembershipOracle;
import net.automatalib.word.Word;

public class MooreOutInconsPrefixTransformAcex<I, O> extends OutInconsPrefixTransformAcex<I, Word<O>> {

    public MooreOutInconsPrefixTransformAcex(Word<I> suffix,
                                             MembershipOracle<I, Word<O>> oracle,
                                             Function<Word<I>, Word<I>> asTransform) {
        super(suffix, suffix.length() + 1, oracle, asTransform);
    }

    @Override
    public boolean checkEffects(Word<O> eff1, Word<O> eff2) {
        return eff2.isSuffixOf(eff1);
    }

}
