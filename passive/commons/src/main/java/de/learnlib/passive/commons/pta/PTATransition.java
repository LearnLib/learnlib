package de.learnlib.passive.commons.pta;

import java.util.Objects;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PTATransition<S extends BasePTAState<?, ?, S>> {
	
	private final S source;
	private final int index;

	public PTATransition(@Nonnull S source, @Nonnegative int index) {
		this.source = Objects.requireNonNull(source);
		if (index < 0) {
			throw new IllegalArgumentException();
		}
		this.index = index;
	}
	
	@Nonnull
	public S getSource() {
		return source;
	}
	
	@Nonnegative
	public int getIndex() {
		return index;
	}
	
	@Nullable
	public S getTarget() {
		return source.getSuccessor(index);
	}

}
