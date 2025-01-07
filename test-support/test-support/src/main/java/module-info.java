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
 * This module provides support classes for easily writing unit tests for various components of LearnLib. <b>Note:</b>
 * This module is not intended as a library but only exists for internal testing purposes. You may use it but
 * documentation may be sparse and usability may be inconvenient without any intentions to change it.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;de.learnlib.testsupport&lt;/groupId&gt;
 *   &lt;artifactId&gt;learnlib-test-support&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module de.learnlib.testsupport {

    requires de.learnlib.api;
    requires de.learnlib.common.util;
    requires de.learnlib.driver.simulator;
    requires de.learnlib.oracle.membership;
    requires de.learnlib.testsupport.example;
    requires net.automatalib.api;
    requires net.automatalib.core;
    requires net.automatalib.util;
    requires org.apache.fury.core;
    requires org.mockito;
    requires org.testng;

    // annotations are 'provided'-scoped and do not need to be loaded at runtime
    requires static org.checkerframework.checker.qual;

    exports de.learnlib.testsupport;
}
