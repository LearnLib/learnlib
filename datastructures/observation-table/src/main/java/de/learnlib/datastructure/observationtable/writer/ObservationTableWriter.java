/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.datastructure.observationtable.writer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.WillNotClose;

import de.learnlib.datastructure.observationtable.ObservationTable;
import net.automatalib.commons.util.IOUtil;

@ParametersAreNonnullByDefault
public interface ObservationTableWriter<I, D> {

    void write(ObservationTable<? extends I, ? extends D> table, @WillNotClose Appendable out) throws IOException;

    default void write(ObservationTable<? extends I, ? extends D> table, @WillNotClose PrintStream out) {
        try {
            write(table, (Appendable) out);
        } catch (IOException ex) {
            throw new AssertionError("Writing to PrintStream must not throw");
        }
    }

    default void write(ObservationTable<? extends I, ? extends D> table, @WillNotClose StringBuilder out) {
        try {
            write(table, (Appendable) out);
        } catch (IOException ex) {
            throw new AssertionError("Writing to StringBuilder must not throw");
        }
    }

    default void write(ObservationTable<? extends I, ? extends D> table, File file) throws IOException {
        try (Writer w = IOUtil.asBufferedUTF8Writer(file)) {
            write(table, w);
        }
    }
}
