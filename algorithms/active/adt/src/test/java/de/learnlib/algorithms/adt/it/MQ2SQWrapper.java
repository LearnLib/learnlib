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
package de.learnlib.algorithms.adt.it;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.SymbolQueryOracle;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * @author frohme
 */
public class MQ2SQWrapper<I, O> implements SymbolQueryOracle<I, O> {

    final WordBuilder<I> wb;
    final MembershipOracle<I, Word<O>> oracle;

    public MQ2SQWrapper(final MembershipOracle<I, Word<O>> oracle) {
        this.oracle = oracle;
        this.wb = new WordBuilder<>();
    }

    @Override
    public O query(I i) {
        this.wb.append(i);
        return this.oracle.answerQuery(wb.toWord()).lastSymbol();
    }

    @Override
    public void reset() {
        this.wb.clear();
    }
}
