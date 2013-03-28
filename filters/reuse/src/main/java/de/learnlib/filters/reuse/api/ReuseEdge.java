package de.learnlib.filters.reuse.api;


/**
 * A {@link ReuseEdge} connects two vertices in the {@link ReuseTree} and is
 * labeled with input and output behaviour.
 *
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class ReuseEdge {
	private final ReuseNode source, target;
	private final String input, output;

	public ReuseEdge(ReuseNode source, ReuseNode target, String input, String output) {
		if (source == null) {
			throw new IllegalArgumentException("Source not allowed to be null.");
		}
		if (target == null) {
			throw new IllegalArgumentException("Target not allowed to be null.");
		}
		if (input == null) {
			throw new IllegalArgumentException("Input not allowed to be null.");
		}
		if (output == null) {
			throw new IllegalArgumentException("Output not allowed to be null.");
		}
		this.source = source;
		this.target = target;
		this.input = input;
		this.output = output;
	}

	public ReuseNode getSource() {
		return source;
	}

	public ReuseNode getTarget() {
		return target;
	}

	public String getInput() {
		return input;
	}

	public String getOutput() {
		return output;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(source.toString()).append(source);
		sb.append(" -> ").append(target).append(" i/o ").append(input).append("/").append(output);
		return sb.toString();
	}
}
