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
 * This module provides the implementations of various learning algorithms for systems of procedural automata such as
 * the ones described in the papers <a href="https://doi.org/10.1007/s10009-021-00634-y">Compositional learning of
 * mutually recursive procedural systems</a> and <a href="https://doi.org/10.1007/978-3-031-15629-8_11">From Languages
 * to Behaviors and Back</a>  by Markus Frohme and Bernhard Steffen.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;de.learnlib&lt;/groupId&gt;
 *   &lt;artifactId&gt;learnlib-procedural&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module de.learnlib.algorithm.procedural {

    requires de.learnlib.algorithm.kv;
    requires de.learnlib.algorithm.lambda;
    requires de.learnlib.algorithm.lstar;
    requires de.learnlib.algorithm.observationpack;
    requires de.learnlib.algorithm.ttt;
    requires de.learnlib.api;
    requires de.learnlib.common.util;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.datastructure;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.util;

    // annotations are 'provided'-scoped and do not need to be loaded at runtime
    requires static org.checkerframework.checker.qual;

    exports de.learnlib.algorithm.procedural;
    exports de.learnlib.algorithm.procedural.adapter.dfa;
    exports de.learnlib.algorithm.procedural.adapter.mealy;
    exports de.learnlib.algorithm.procedural.sba;
    exports de.learnlib.algorithm.procedural.sba.manager;
    exports de.learnlib.algorithm.procedural.spa;
    exports de.learnlib.algorithm.procedural.spa.manager;
    exports de.learnlib.algorithm.procedural.spmm;
    exports de.learnlib.algorithm.procedural.spmm.manager;
}
