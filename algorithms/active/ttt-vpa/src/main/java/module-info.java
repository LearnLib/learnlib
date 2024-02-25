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

/**
 * This module provides the implementation of the VPA adaption of the TTT learning algorithm as
 * presented in the PhD thesis <a href="https://dx.doi.org/10.17877/DE290R-16359">Foundations of Active Automata
 * Learning: An Algorithmic Perspective</a> by Malte Isberner.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;de.learnlib&lt;/groupId&gt;
 *   &lt;artifactId&gt;learnlib-ttt-vpa&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module de.learnlib.algorithm.ttt.vpa {

    requires de.learnlib.api;
    requires de.learnlib.algorithm.observationpack.vpa;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.datastructure.discriminationtree;
    requires net.automatalib.api;
    requires net.automatalib.common.util;

    requires static de.learnlib.tooling.annotation;
    // make non-static once https://github.com/typetools/checker-framework/issues/4559 is implemented
    requires static org.checkerframework.checker.qual;

    exports de.learnlib.algorithm.ttt.vpa;
}
