/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.api.logging;

import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLogger;

/**
 * A simple {@link LearnLogger} implementation, that delegates all calls to a given {@link Logger} instance.
 *
 * @author frohme
 */
public class Slf4jDelegator extends SubstituteLogger implements LearnLogger {

    @SuppressWarnings("nullness") // we don't need to provide a queue instance, because the delegate will always be used
    public Slf4jDelegator(final Logger delegate) {
        super(delegate.getName(), null, false);
        super.setDelegate(delegate);
    }

}
