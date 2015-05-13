package de.learnlib.passive.rpni;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AbstractBlueFringePTA<SP,TP,S extends AbstractBlueFringePTAState<SP,TP,S>>
		extends AbstractPTA<SP,TP,S> {
	
	@Nonnull
	protected final List<S> redStates = new ArrayList<>();

	public AbstractBlueFringePTA(@Nonnegative int alphabetSize, S root) {
		super(alphabetSize, root);
	}
	
	public S getRedState(@Nonnegative int id) {
		return redStates.get(id);
	}
	
	@Nonnegative
	public int getNumRedStates() {
		return redStates.size();
	}
	
	public void init(Consumer<? super BlueStateRef<S>> newBlue) {
		root.color = Color.BLUE;
		promote(root, newBlue);
	}
	
	private void makeRed(S qb) {
		if (!qb.isBlue()) {
			throw new IllegalArgumentException();
		}
		qb.makeRed(redStates.size());
		redStates.add(qb);
	}
	
	public void promote(S qb, Consumer<? super BlueStateRef<S>> newBlue) {
		makeRed(qb);
		qb.forEachSucc(s -> newBlue.accept(s.makeBlue()));
	}
	
	public RedBlueMerge<SP, TP, S> tryMerge(S qr, S qb) {
		RedBlueMerge<SP, TP, S> merge = new RedBlueMerge<>(this, qr, qb);
		if (!merge.merge()) {
			return null;
		}
		return merge;
 	}

}
