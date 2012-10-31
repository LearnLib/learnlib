package de.ls5.learnlib.os.api;

import java.util.Collection;

/**
 *
 * @author merten
 */
public interface CEXHandlerPrefixes<W> {
	
	public Collection<W> createPrefixes(W counterexample);
	
}
