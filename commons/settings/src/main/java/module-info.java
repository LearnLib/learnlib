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

import de.learnlib.setting.LearnLibSettingsSource;
import de.learnlib.setting.sources.LearnLibLocalPropertiesAutomataLibSettingsSource;
import de.learnlib.setting.sources.LearnLibLocalPropertiesSource;
import de.learnlib.setting.sources.LearnLibPropertiesAutomataLibSettingsSource;
import de.learnlib.setting.sources.LearnLibPropertiesSource;
import de.learnlib.setting.sources.LearnLibSystemPropertiesAutomataLibSettingsSource;
import de.learnlib.setting.sources.LearnLibSystemPropertiesSource;
import net.automatalib.common.setting.AutomataLibSettingsSource;

/**
 * This module provides a collection of utility methods to parse LearnLib specific settings.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;de.learnlib&lt;/groupId&gt;
 *   &lt;artifactId&gt;learnlib-settings&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module de.learnlib.setting {

    requires de.learnlib.api;
    requires net.automatalib.common.setting;
    requires net.automatalib.common.util;
    requires org.slf4j;

    // annotations are 'provided'-scoped and do not need to be loaded at runtime
    requires static org.checkerframework.checker.qual;
    requires static org.kohsuke.metainf_services;

    exports de.learnlib.setting;
    exports de.learnlib.setting.sources;

    uses LearnLibSettingsSource;

    provides LearnLibSettingsSource with LearnLibLocalPropertiesSource, LearnLibPropertiesSource, LearnLibSystemPropertiesSource;
    provides AutomataLibSettingsSource with LearnLibLocalPropertiesAutomataLibSettingsSource, LearnLibPropertiesAutomataLibSettingsSource, LearnLibSystemPropertiesAutomataLibSettingsSource;
}
