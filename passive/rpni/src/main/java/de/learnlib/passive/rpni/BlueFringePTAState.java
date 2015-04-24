package de.learnlib.passive.rpni;

public class BlueFringePTAState<SP,TP> extends AbstractBlueFringePTAState<SP, TP, BlueFringePTAState<SP,TP>> {
	@Override
	protected BlueFringePTAState<SP, TP> createState() {
		return new BlueFringePTAState<>();
	}
}
