package de.learnlib.examples.aaar;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import de.learnlib.examples.aaar.Event.Msg;
import de.learnlib.examples.aaar.Event.Recv;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;

class Protocol implements SuffixOutput<Event, Word<String>> {

    private int seq_exp;
    private Object buffer;

    @Override
    public Word<String> computeSuffixOutput(Iterable<? extends Event> prefix, Iterable<? extends Event> suffix) {

        System.err.println(Iterables.toString(prefix) + "|" + Iterables.toString(suffix));
        reset();
        prefix.forEach(this::handleEvent);
        return Streams.stream(suffix).map(this::handleEvent).collect(Word.collector());
    }

    private void reset() {
        seq_exp = 0;
        buffer = null;
    }

    private String handleEvent(Event event) {
        if (event instanceof Msg<?>) {
            Msg<?> msg = (Msg<?>) event;

            if (buffer == null && msg.seq % 2 == 0 && seq_exp % 2 == 0) {
                buffer = msg.data;
                seq_exp++;
                return "ind";
            }

            return "-";
        } else if (event instanceof Recv) {
            Recv recv = (Recv) event;
            if (buffer == null) {
                return "-";
            }
            int ack = (seq_exp - 1) % 2;
            buffer = null;
            return "ack(" + ack + ')';
        } else {
            throw new IllegalStateException();
        }
    }
}
