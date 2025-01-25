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
 * This module provides a collection of various small example applications that illustrate several use cases of
 * LearnLib.
 * <b>Note:</b> This module is not intended as a library but only exists for educational purposes. No artifacts are
 * deployed for this module.
 */
open module de.learnlib.example {

    requires java.desktop;
    requires de.learnlib.algorithm.ttt;
    requires de.learnlib.api;
    requires de.learnlib.algorithm.aaar;
    requires de.learnlib.algorithm.lstar;
    requires de.learnlib.algorithm.rpni;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.common.util;
    requires de.learnlib.datastructure;
    requires de.learnlib.driver;
    requires de.learnlib.driver.simulator;
    requires de.learnlib.filter.cache;
    requires de.learnlib.filter.reuse;
    requires de.learnlib.filter.statistic;
    requires de.learnlib.oracle.emptiness;
    requires de.learnlib.oracle.equivalence;
    requires de.learnlib.oracle.membership;
    requires de.learnlib.oracle.parallelism;
    requires de.learnlib.oracle.property;
    requires de.learnlib.testsupport.example;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.modelchecker.ltsmin;
    requires net.automatalib.util;
    requires net.automatalib.serialization.dot;
    requires net.automatalib.visualization.dot;
    requires org.apache.fury.core;

    // required by Fury
    requires jdk.unsupported;
    requires java.sql;

    // annotations are 'provided'-scoped and do not need to be loaded at runtime
    requires static org.checkerframework.checker.qual;

    exports de.learnlib.example;
    exports de.learnlib.example.aaar;
    exports de.learnlib.example.bbc;
    exports de.learnlib.example.parallelism;
    exports de.learnlib.example.passive;
    exports de.learnlib.example.resumable;
    exports de.learnlib.example.sli;
}
