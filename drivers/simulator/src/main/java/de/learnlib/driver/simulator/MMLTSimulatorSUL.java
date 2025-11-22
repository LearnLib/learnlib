package de.learnlib.driver.simulator;

import de.learnlib.sul.TimedSUL;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Simulates the extended semantics of an MMLT.
 *
 * @param <S> Location type.
 * @param <I> Non-delaying input type.
 * @param <O> Output symbol type.
 */
public class MMLTSimulatorSUL<S, I, T, O> implements TimedSUL<I, O> {

    private final MMLTSemantics<S, I, T, O> semantics;

    private State<S, O> currentConfiguration;

    public MMLTSimulatorSUL(MMLTSemantics<S, I, T, O> semantics) {
        this.semantics = semantics;
        this.currentConfiguration = null;
    }


    @Override
    public TimedOutput<O> step(InputSymbol<I> input) {
        if (this.currentConfiguration == null) {
            throw new IllegalStateException("Not initialized!");
        }

        var trans = this.semantics.getTransition(this.currentConfiguration, input);
        this.currentConfiguration = this.semantics.getSuccessor(trans);
        return this.semantics.getTransitionOutput(trans);
    }

    @Override
    public @Nullable TimedOutput<O> timeoutStep(long maxTime) {
        if (this.currentConfiguration == null) {
            throw new IllegalStateException("Not initialized!");
        }

        var trans = this.semantics.getTransition(this.currentConfiguration, new TimeoutSymbol<>(), maxTime);
        this.currentConfiguration = this.semantics.getSuccessor(trans);
        var output = this.semantics.getTransitionOutput(trans);

        if (output.equals(semantics.getSilentOutput())) {
            // No timeout observed:
            return null;
        } else {
            return output;
        }
    }

    @Override
    public void pre() {
        this.currentConfiguration = semantics.getInitialState();
    }

    @Override
    public void post() {
        this.currentConfiguration = null;
    }


}
