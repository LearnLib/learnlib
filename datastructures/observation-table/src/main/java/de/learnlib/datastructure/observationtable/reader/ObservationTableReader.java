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
package de.learnlib.datastructure.observationtable.reader;

import javax.annotation.Nonnull;

import de.learnlib.datastructure.observationtable.ObservationTable;
import net.automatalib.words.Alphabet;

/**
 * Reads an {@link ObservationTable} from a string source.
 *
 * @param <I>
 *         input symbol class
 * @param <D>
 *         observation (output) domain class
 */
public interface ObservationTableReader<I, D> {

    /**
     * Reads the string representation of an observation table and returns an implementation of {@link ObservationTable}
     * which allows access to short and long prefixes as well as suffixes.
     *
     * @param source
     *         The string representation to read, must not be {@code null}.
     * @param alphabet
     *         The learning alphabet containing the symbols used, must not be {@code null}.
     *
     * @return an implementation of {@link ObservationTable} which allows at lease access to {@link
     * ObservationTable#getSuffixes()}, {@link ObservationTable#getShortPrefixes()}, and {@link
     * ObservationTable#getLongPrefixes()}. Will never be {@code null}.
     */
    @Nonnull
    ObservationTable<I, D> read(@Nonnull String source, @Nonnull Alphabet<I> alphabet);

}
