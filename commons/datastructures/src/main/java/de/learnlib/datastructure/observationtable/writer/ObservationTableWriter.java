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
package de.learnlib.datastructure.observationtable.writer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

import de.learnlib.datastructure.observationtable.ObservationTable;
import net.automatalib.common.util.IOUtil;

public interface ObservationTableWriter<I, D> {

    void write(ObservationTable<? extends I, ? extends D> table, Appendable out) throws IOException;

    default void write(ObservationTable<? extends I, ? extends D> table, PrintStream out) {
        try {
            write(table, (Appendable) out);
        } catch (IOException ex) {
            throw new AssertionError("Writing to PrintStream must not throw", ex);
        }
    }

    default void write(ObservationTable<? extends I, ? extends D> table, StringBuilder out) {
        try {
            write(table, (Appendable) out);
        } catch (IOException ex) {
            throw new AssertionError("Writing to StringBuilder must not throw", ex);
        }
    }

    default void write(ObservationTable<? extends I, ? extends D> table, File file) throws IOException {
        try (Writer w = IOUtil.asBufferedUTF8Writer(file)) {
            write(table, w);
        }
    }
}
