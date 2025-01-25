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
 * This module provides the implementation of the L* learning algorithm described in the paper
 * <a href="https://doi.org/10.1016/0890-5401(87)90052-6">Learning Regular Sets from Queries and Counterexamples</a> by
 * Dana Angluin including variations and optimizations thereof such as the versions based on <a
 * href="https://doi.org/10.1006/inco.1995.1070">"On the Learnability of Infinitary Regular Sets</a> by Oded Maler and
 * Amir Pnueli or <a href="http://doi.org/10.1006/inco.1993.1021">Inference of finite automata using homing
 * sequences</a>) by Ronald L.&nbsp;Rivest and Robert E.&nbsp;Schapire.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;de.learnlib&lt;/groupId&gt;
 *   &lt;artifactId&gt;learnlib-lstar&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module de.learnlib.algorithm.lstar {

    requires de.learnlib.api;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.common.util;
    requires de.learnlib.datastructure;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires org.slf4j;

    // annotations are 'provided'-scoped and do not need to be loaded at runtime
    requires static de.learnlib.tooling.annotation;
    requires static org.checkerframework.checker.qual;

    exports de.learnlib.algorithm.lstar;
    exports de.learnlib.algorithm.lstar.ce;
    exports de.learnlib.algorithm.lstar.closing;
    exports de.learnlib.algorithm.lstar.dfa;
    exports de.learnlib.algorithm.lstar.mealy;
    exports de.learnlib.algorithm.lstar.moore;
    exports de.learnlib.algorithm.malerpnueli;
    exports de.learnlib.algorithm.rivestschapire;
}
