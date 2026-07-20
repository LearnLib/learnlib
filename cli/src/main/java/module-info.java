/* Copyright (C) 2013-2026 TU Dortmund University
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
open module de.learnlib.cli {

    requires de.learnlib.algorithm.dhc;
    requires de.learnlib.algorithm.kv;
    requires de.learnlib.algorithm.lambda;
    requires de.learnlib.algorithm.lstar;
    requires de.learnlib.algorithm.nlstar;
    requires de.learnlib.algorithm.observationpack;
    requires de.learnlib.algorithm.observationpack.vpa;
    requires de.learnlib.algorithm.procedural;
    requires de.learnlib.algorithm.sparse;
    requires de.learnlib.algorithm.ttt;
    requires de.learnlib.algorithm.ttt.vpa;
    requires de.learnlib.api;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.common.util;
    requires de.learnlib.filter.cache;
    requires de.learnlib.filter.statistic;
    requires de.learnlib.oracle.equivalence;
    requires de.learnlib.oracle.membership;

    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.serialization.dot;
    requires net.automatalib.serialization.saf;
    requires net.automatalib.serialization.mata;
    requires net.automatalib.serialization.aut;
    requires net.automatalib.serialization.ba;
    requires net.automatalib.serialization.learnlibv2;
    requires net.automatalib.util;

    requires ch.qos.logback.classic;
    requires info.picocli;
    requires org.slf4j;

}
