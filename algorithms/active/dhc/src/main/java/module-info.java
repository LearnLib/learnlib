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

/**
 * This module provides the implementation of the DHC learning algorithm as described in the paper
 * <a href="https://doi.org/10.1007/978-3-642-34781-8_19">Automata Learning with on-the-Fly Direct Hypothesis
 * Construction</a> by Maik Merten, Falk Howar, Bernhard Steffen, and Tiziana Margaria.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;de.learnlib&lt;/groupId&gt;
 *   &lt;artifactId&gt;learnlib-dhc&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module de.learnlib.algorithm.dhc {

    requires de.learnlib.api;
    requires de.learnlib.common.counterexample;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;

    // annotations are 'provided'-scoped and do not need to be loaded at runtime
    requires static de.learnlib.tooling.annotation;
    requires static org.checkerframework.checker.qual;

    exports de.learnlib.algorithm.dhc.mealy;
}
