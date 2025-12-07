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
 * This module provides data structures shared by multiple learning algorithms of LearnLib.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;de.learnlib&lt;/groupId&gt;
 *   &lt;artifactId&gt;learnlib-datastructures&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module de.learnlib.datastructure {

    requires de.learnlib.api;
    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.common.util;
    requires net.automatalib.core;

    // annotations are 'provided'-scoped and do not need to be loaded at runtime
    requires static org.checkerframework.checker.qual;

    exports de.learnlib.datastructure.discriminationtree;
    exports de.learnlib.datastructure.discriminationtree.iterators;
    exports de.learnlib.datastructure.discriminationtree.model;
    exports de.learnlib.datastructure.list;
    exports de.learnlib.datastructure.observationtable;
    exports de.learnlib.datastructure.observationtable.reader;
    exports de.learnlib.datastructure.observationtable.writer;
    exports de.learnlib.datastructure.pta;
    exports de.learnlib.datastructure.pta.config;
    exports de.learnlib.datastructure.pta.wrapper;
    exports de.learnlib.datastructure.pta.visualization;
}
