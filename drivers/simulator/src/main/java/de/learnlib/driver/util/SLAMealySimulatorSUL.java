package de.learnlib.driver.util;

import java.util.Collection;

import de.learnlib.api.StateLocalInputSUL;
import de.learnlib.api.exception.SULException;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;

public class SLAMealySimulatorSUL<I, O> extends MealySimulatorSUL<I, O> implements StateLocalInputSUL<I, O> {

    protected final SLAMealySimulatorSULImpl<?, I, ?, O> impl;

    public SLAMealySimulatorSUL(StateLocalInputMealyMachine<?, I, ?, O> mealy) {
        this(new SLAMealySimulatorSULImpl<>(mealy));
    }

    private SLAMealySimulatorSUL(SLAMealySimulatorSULImpl<?, I, ?, O> impl) {
        super(impl);
        this.impl = impl;
    }

    @Override
    public StateLocalInputSUL<I, O> fork() {
        return new SLAMealySimulatorSUL<>(impl.fork());
    }

    @Override
    public Collection<I> currentlyEnabledInputs() throws SULException {
        return this.impl.currentlyEnabledInputs();
    }

    private static final class SLAMealySimulatorSULImpl<S, I, T, O> extends MealySimulatorSULImpl<S, I, T, O>
            implements StateLocalInputSUL<I, O> {

        private final StateLocalInputMealyMachine<S, I, T, O> mealy;

        SLAMealySimulatorSULImpl(StateLocalInputMealyMachine<S, I, T, O> mealy) {
            super(mealy, null);
            this.mealy = mealy;
        }

        @Override
        public Collection<I> currentlyEnabledInputs() throws SULException {
            return this.mealy.getLocalInputs(super.getCurr());
        }

        @Override
        public SLAMealySimulatorSULImpl<S, I, T, O> fork() {
            return new SLAMealySimulatorSULImpl<>(mealy);
        }
    }
}
