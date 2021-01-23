/* Copyright (C) 2013-2021 TU Dortmund
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
package de.learnlib.testsupport;

import de.learnlib.api.Resumable;
import org.nustaq.serialization.FSTConfiguration;

/**
 * Utility functions for {@link Resumable} features.
 *
 * @author frohme
 */
public final class ResumeUtils {

    private static final FSTConfiguration FST_CONFIGURATION;

    static {
        FST_CONFIGURATION = FSTConfiguration.createDefaultConfiguration();
        FST_CONFIGURATION.setForceSerializable(true);
        FST_CONFIGURATION.setShareReferences(true);
    }

    private ResumeUtils() {
        // prevent instantiation
    }

    public static <T> byte[] toBytes(T state) {
        try {
            return FST_CONFIGURATION.asByteArray(state);
        } finally {
            FST_CONFIGURATION.clearCaches();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromBytes(byte[] bytes) {
        return (T) FST_CONFIGURATION.asObject(bytes);
    }

}
