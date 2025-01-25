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
package de.learnlib.datastructure.observationtable.writer;

import java.util.Objects;
import java.util.function.Function;

import net.automatalib.word.Word;

public abstract class AbstractObservationTableWriter<I, D> implements ObservationTableWriter<I, D> {

    private final Function<? super Word<? extends I>, ? extends String> wordToString;
    private final Function<? super D, ? extends String> outputToString;

    public AbstractObservationTableWriter() {
        this(Objects::toString, Objects::toString);
    }

    public AbstractObservationTableWriter(Function<? super Word<? extends I>, ? extends String> wordToString,
                                          Function<? super D, ? extends String> outputToString) {
        this.wordToString = safeToStringFunction(wordToString);
        this.outputToString = safeToStringFunction(outputToString);
    }

    protected static <T> Function<? super T, ? extends String> safeToStringFunction(Function<? super T, ? extends String> toStringFunction) {
        if (toStringFunction != null) {
            return toStringFunction;
        }
        return Objects::toString;
    }

    protected String wordToString(Word<? extends I> word) {
        return wordToString.apply(word);
    }

    protected String outputToString(D output) {
        return outputToString.apply(output);
    }

}
