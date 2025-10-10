package de.learnlib.driver.simulator;

import de.learnlib.sul.LocalTimerMealySUL;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.alphabet.time.mmlt.TimeoutSymbol;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.automaton.time.mmlt.semantics.LocalTimerMealyConfiguration;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Simulates the extended semantics of an MMLT.
 *
 * @param <S> Location type.
 * @param <I> Non-delaying input type.
 * @param <O> Output symbol type.
 */
public class LocalTimerMealySimulatorSUL<S, I, O> implements LocalTimerMealySUL<I, O> {

    private final LocalTimerMealy<S, I, O> automaton;

    private LocalTimerMealyConfiguration<S, I, O> currentConfiguration;


    public LocalTimerMealySimulatorSUL(LocalTimerMealy<S, I, O> automaton) {
        this.automaton = automaton;
        this.currentConfiguration = null;
    }


    @Override
    public LocalTimerMealyOutputSymbol<O> step(NonDelayingInput<I> input) {
        if (this.currentConfiguration == null) {
            throw new IllegalStateException("Not initialized!");
        }

        var trans = this.automaton.getSemantics().getTransition(this.currentConfiguration, input);
        this.currentConfiguration = trans.target();
        return trans.output();
    }

    @Override
    public @Nullable LocalTimerMealyOutputSymbol<O> timeoutStep(long maxTime) {
        if (this.currentConfiguration == null) {
            throw new IllegalStateException("Not initialized!");
        }

        var trans = this.automaton.getSemantics().getTransition(this.currentConfiguration, new TimeoutSymbol<>(), maxTime);
        this.currentConfiguration = trans.target();

        if (trans.output().equals(automaton.getSemantics().getSilentOutput())) {
            // No timeout observed:
            return null;
        } else {
            return trans.output();
        }
    }

    @Override
    public void pre() {
        this.currentConfiguration = automaton.getSemantics().getInitialConfiguration().copy();
    }

    @Override
    public void post() {
        this.currentConfiguration = null;
    }


}
