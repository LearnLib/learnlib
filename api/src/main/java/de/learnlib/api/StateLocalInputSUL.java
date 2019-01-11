package de.learnlib.api;

import java.util.Collection;

import de.learnlib.api.exception.SULException;

/**
 * A System Under Learning (SUL) which can additionally report the inputs that the SUL can process in its current state,
 * i.e. inputs that will not trigger a {@link SULException} when used in the next invocation of the {@link
 * #step(Object)} method return an otherwise "undefined" behavior.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author Maren Geske
 * @author frohme
 */
public interface StateLocalInputSUL<I, O> extends SUL<I, O> {

    Collection<I> currentlyEnabledInputs();

    @Override
    default StateLocalInputSUL<I, O> fork() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
