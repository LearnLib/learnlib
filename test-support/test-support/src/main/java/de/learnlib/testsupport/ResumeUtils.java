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
package de.learnlib.testsupport;

import de.learnlib.Resumable;
import org.apache.fory.Fory;
import org.apache.fory.logging.LoggerFactory;
import org.apache.fory.resolver.AllowListChecker;
import org.apache.fory.resolver.AllowListChecker.CheckLevel;

/**
 * Utility functions for {@link Resumable} features.
 */
public final class ResumeUtils {

    private static final Fory FORY;

    static {
        // use same config as CLI to automatically test proper white-listing
        final AllowListChecker checker = new AllowListChecker();
        checker.setCheckLevel(CheckLevel.STRICT);
        checker.allowClass("de.learnlib.*");
        checker.allowClass("net.automatalib.*");
        FORY = Fory.builder()
                   .requireClassRegistration(false)
                   .withCodegen(false)
                   .withRefTracking(true)
                   .withTypeChecker(checker)
                   .withXlang(false)
                   .build();
        LoggerFactory.useSlf4jLogging(true);
    }

    private ResumeUtils() {
        // prevent instantiation
    }

    public static byte[] toBytes(Object state) {
        return FORY.serialize(state);
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromBytes(byte[] bytes) {
        return (T) FORY.deserialize(bytes);
    }

}
