package de.learnlib.passive.rpni;

public class BlueFringePTA<SP, TP> extends AbstractBlueFringePTA<SP, TP, BlueFringePTAState<SP,TP>> {
	public BlueFringePTA(int alphabetSize) {
		super(alphabetSize, new BlueFringePTAState<SP, TP>());
	}
}
