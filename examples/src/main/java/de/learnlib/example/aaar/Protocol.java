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
package de.learnlib.example.aaar;

import java.util.stream.StreamSupport;

import de.learnlib.example.aaar.Event.Msg;
import de.learnlib.example.aaar.Event.Recv;
import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleMealy;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

class Protocol implements SingleQueryOracleMealy<Event, String> {

    private int seqExp;
    private @Nullable Object buffer;

    @Override
    public Word<String> answerQuery(Word<Event> prefix, Word<Event> suffix) {
        reset();
        prefix.forEach(this::handleEvent);
        return StreamSupport.stream(suffix.spliterator(), false).map(this::handleEvent).collect(Word.collector());
    }

    private void reset() {
        seqExp = 0;
        buffer = null;
    }

    private String handleEvent(Event event) {
        if (event instanceof Msg<?>) {
            Msg<?> msg = (Msg<?>) event;

            if (buffer == null && msg.seq % 2 == seqExp % 2) {
                buffer = msg.data;
                seqExp++;
                return "ind";
            }

            return "-";
        } else if (event instanceof Recv) {
            if (buffer == null) {
                return "-";
            }
            int ack = (seqExp - 1) % 2;
            buffer = null;
            return "ack(" + ack + ')';
        } else {
            throw new IllegalArgumentException("Unknown event: " + event);
        }
    }
}
