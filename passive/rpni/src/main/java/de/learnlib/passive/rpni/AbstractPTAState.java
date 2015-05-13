package de.learnlib.passive.rpni;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.automatalib.commons.util.array.RichArray;

public abstract class AbstractPTAState<SP,TP,S extends AbstractPTAState<SP,TP,S>> implements Cloneable {

	protected SP property;
	protected RichArray<TP> transProperties;
	protected RichArray<S> successors;
	protected int id = -1;
	
	public SP getStateProperty() {
		return property;
	}
	
	public TP getTransProperty(int index) {
		if (transProperties == null) {
			return null;
		}
		return transProperties.get(index);
	}
	
	public S copy() {
		return copy((transProperties != null) ? transProperties.clone() : null);
	}
	
	@SuppressWarnings("unchecked")
	public S copy(RichArray<TP> newTPs) {
		try {
			S copy = (S) clone();
			copy.transProperties = newTPs;
			if (successors != null) {
				copy.successors = successors.clone();
			}
			return copy;
		}
		catch (CloneNotSupportedException ex) {
			throw new AssertionError(ex);
		}
	}
	
	public S getSuccessor(int index) {
		if (successors == null) {
			return null;
		}
		return successors.get(index);
	}
	
	public void setSuccessor(int index, S succ, int alphabetSize) {
		if (successors == null) {
			successors = new RichArray<>(alphabetSize);
		}
		successors.update(index, succ);
	}
	
	public S getOrCreateSuccessor(int index, int alphabetSize) {
		if (successors == null) {
			successors = new RichArray<>(alphabetSize);
		}
		S succ = successors.get(index);
		if (succ == null) {
			succ = createSuccessor(index);
			successors.update(index, succ);
		}
		return succ;
	}

	@SuppressWarnings("unchecked")
	public void forEachSucc(Consumer<? super S> cons) {
		if (successors != null) {
			for (Object succ : successors) {
				if (succ != null) {
					cons.accept((S) succ);
				}
			}
		}
	}

	
	public boolean tryMergeStateProperty(SP newSP) {
		if (property != null) {
			if (!Objects.equals(property, newSP)) {
				return false;
			}
			return true;
		}
		this.property = newSP;
		return true;
	}
	
	public void mergeStateProperty(SP newSP) {
		if (!tryMergeStateProperty(newSP)) {
			throw new IllegalStateException();
		}
	}
	
	public Stream<S> successors() {
		if (successors == null) {
			return Stream.empty();
		}
		return successors.stream().filter(x -> x != null);
	}
	
	protected S createSuccessor(int index) {
		return createState();
	}
	
	protected abstract S createState();
}
