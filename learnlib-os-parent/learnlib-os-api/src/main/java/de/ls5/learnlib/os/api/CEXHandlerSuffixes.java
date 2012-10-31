package de.ls5.learnlib.os.api;

import java.util.Collection;

/**
 *
 * @author merten
 */
public interface CEXHandlerSuffixes<W> {

	public Collection<W> createSuffixes(W counterexample);
	
}
