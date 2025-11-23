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
package de.learnlib.time;

import net.automatalib.automaton.mmlt.SymbolCombiner;

/**
 * Model-specific parameters for MMLT-based learners. These are used by various filters, oracles, and the MMLT
 * simulator.
 *
 * @param silentOutput
 *         Silent output symbol
 * @param outputCombiner
 *         Function for combining simultaneously occurring outputs of timers
 * @param maxTimeoutWaitingTime
 *         Maximum time to wait for a timeout in any configuration. If no timeout is observed after this time, the
 *         learner assumes that no timers are active. Hence, if this value is set too low, the learner will miss
 *         timeouts. This usually results in an incomplete model but can also trigger exceptions due to unsatisfied
 *         assumptions.
 * @param maxTimerQueryWaitingTime
 *         Maximum waiting time to wait when inferring timers for a location. This must be at least the max. time for a
 *         timeout. We recommend setting this value to at least twice the highest value of any timer in the SUL, if
 *         these values are known or can be estimated. This increases the likelihood of detecting non-periodic behavior
 *         during timer inference, and thus reduces the need for equivalence queries.
 * @param <O>
 *         Output symbol type
 */
public record MMLTModelParams<O>(O silentOutput, SymbolCombiner<O> outputCombiner, long maxTimeoutWaitingTime,
                                 long maxTimerQueryWaitingTime) {}
