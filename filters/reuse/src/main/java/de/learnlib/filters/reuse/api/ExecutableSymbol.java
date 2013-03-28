package de.learnlib.filters.reuse.api;

/**
 * Any class that is marked with this interface can be used for
 * inferring behavior by executing the corresponding <code>execute</code>
 * method. Please note that the {@link Oracle}s also need special
 * implementations.
 *
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public interface ExecutableSymbol {
	String execute(SystemState state);
}
