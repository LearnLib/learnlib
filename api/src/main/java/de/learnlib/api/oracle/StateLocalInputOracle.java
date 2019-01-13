/* Copyright (C) 2013-2019 TU Dortmund
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
package de.learnlib.api.oracle;

import java.util.Set;

import net.automatalib.words.Word;

/**
 * A specialization of the {@link MembershipOracle} that adds the possibility to query for what continuations of a word
 * the membership function is still defined. That is, the membership function is undefined for any word, that continues
 * the queried input sequence with a symbol that is not contained in the returned set.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         domain symbol type
 *
 * @author Maren Geske
 * @author frohme
 */
public interface StateLocalInputOracle<I, D> extends MembershipOracle<I, D> {

    Set<I> definedInputs(Word<? extends I> input);

    interface StateLocalInputDFAOracle<I> extends StateLocalInputOracle<I, Boolean>, DFAMembershipOracle<I> {}

    interface StateLocalInputMealyOracle<I, O> extends StateLocalInputOracle<I, Word<O>>, MealyMembershipOracle<I, O> {}
}
