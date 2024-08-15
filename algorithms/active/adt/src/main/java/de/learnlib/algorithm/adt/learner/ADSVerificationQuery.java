package de.learnlib.algorithm.adt.learner;

import java.util.Objects;

import de.learnlib.algorithm.adt.automaton.ADTState;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.DefaultQuery;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

class ADSVerificationQuery<I, O> implements AdaptiveQuery<I, O> {

    private final Word<I> prefix;
    private final Word<I> suffix;
    private final Word<O> expectedOutput;
    private final WordBuilder<O> outputBuilder;
    private final ADTState<I, O> state;

    private final int prefixLength;
    private final int suffixLength;
    private int idx;
    private @Nullable DefaultQuery<I, Word<O>> counterexample;

    ADSVerificationQuery(Word<I> prefix, Word<I> suffix, Word<O> expectedSuffixOutput, ADTState<I, O> state) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.expectedOutput = expectedSuffixOutput;
        this.outputBuilder = new WordBuilder<>(suffix.size());
        this.state = state;

        this.prefixLength = prefix.length();
        this.suffixLength = suffix.length();
        this.idx = 0;
    }

    @Override
    public I getInput() {
        if (idx < prefixLength) {
            return prefix.getSymbol(idx);
        } else {
            return suffix.getSymbol(idx - prefixLength);
        }
    }

    @Override
    public Response processOutput(O out) {
        if (idx < prefixLength) {
            idx++;
            return Response.SYMBOL;
        } else {
            outputBuilder.append(out);

            if (!Objects.equals(out, expectedOutput.getSymbol(idx - prefixLength))) {
                counterexample = new DefaultQuery<>(prefix, suffix.prefix(idx), outputBuilder.toWord());
                return Response.FINISHED;
            } else if (suffixLength < outputBuilder.size()) {
                idx++;
                return Response.SYMBOL;
            } else {
                return Response.FINISHED;
            }
        }
    }

    @Nullable
    DefaultQuery<I, Word<O>> getCounterexample() {
        return counterexample;
    }

    ADTState<I, O> getState() {
        return state;
    }

    Word<I> getSuffix() {
        return suffix;
    }

    Word<O> getExpectedOutput() {
        return expectedOutput;
    }
}
