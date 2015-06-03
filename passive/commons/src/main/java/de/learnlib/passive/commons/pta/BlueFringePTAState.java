package de.learnlib.passive.commons.pta;

public class BlueFringePTAState<SP,TP> extends AbstractBlueFringePTAState<SP, TP, BlueFringePTAState<SP,TP>> {
	@Override
	protected BlueFringePTAState<SP, TP> createState() {
		return new BlueFringePTAState<>();
	}
}
