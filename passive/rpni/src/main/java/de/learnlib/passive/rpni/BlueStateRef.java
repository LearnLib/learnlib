package de.learnlib.passive.rpni;


/**
 * A reference to a blue state in a {@link AbstractBlueFringePTA blue fringe PTA}. This reference does not
 * refer to the instance of the state itself, but rather to its parent (the - persistent - red state), and
 * the corresponding input symbol leading to the referenced blue state.
 * 
 * @author Malte Isberner
 *
 * @param <S> concrete state type
 */
public class BlueStateRef<S extends AbstractBlueFringePTAState<?,?,S>> implements Comparable<BlueStateRef<S>> {
	
	public final S red;
	public final int index;

	public BlueStateRef(S red, int index) {
		assert red.isRed();
		
		this.red = red;
		this.index = index;
	}
	
	public S deref() {
		return red.getSuccessor(index);
	}
	
	@Override
	public int compareTo(BlueStateRef<S> other) {
		int redCmp = red.compareTo(other.red);
		if (redCmp != 0) {
			return redCmp;
		}
		return index - other.index;
	}
	
}
