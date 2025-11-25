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

import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;

/**
 * Represents an output inconsistency used by the MMLT learner.
 *
 * @param prefix
 *         prefix
 * @param suffix
 *         suffix input
 * @param targetOut
 *         suffix output in SUL
 * @param hypOut
 *         suffix output in hypothesis
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public record MMLTOutputInconsistency<I, O>(Word<TimedInput<I>> prefix, Word<TimedInput<I>> suffix,
                                            Word<TimedOutput<O>> targetOut, Word<TimedOutput<O>> hypOut) {}
