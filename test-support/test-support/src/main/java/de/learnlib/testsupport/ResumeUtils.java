/* Copyright (C) 2013-2023 TU Dortmund
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

import java.nio.charset.StandardCharsets;

import com.thoughtworks.xstream.XStream;
import de.learnlib.Resumable;

/**
 * Utility functions for {@link Resumable} features.
 */
public final class ResumeUtils {

    private static final XStream X_STREAM;

    static {
        X_STREAM = new XStream();
        X_STREAM.allowTypesByRegExp(new String[] {"net.automatalib.*", "de.learnlib.*"});
    }

    private ResumeUtils() {
        // prevent instantiation
    }

    public static <T extends Object> byte[] toBytes(T state) {
        return X_STREAM.toXML(state).getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromBytes(byte[] bytes) {
        return (T) X_STREAM.fromXML(new String(bytes, StandardCharsets.UTF_8));
    }

}
