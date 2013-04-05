package de.learnlib.filters.reuse.api;


/**
 * A {@link ReuseEdge} connects two vertices in the {@link ReuseTree} and is
 * labeled with input and output behaviour.
 *
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class ReuseEdge<S,V> {
	private final ReuseNode<S,V> source;
	private final ReuseNode<S,V> target;
	private final S input;
	private final V output;

	public ReuseEdge(ReuseNode<S,V> source, ReuseNode<S,V> target, S input, V output) {
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

	public ReuseNode<S,V> getSource() {
		return source;
	}

	public ReuseNode<S,V> getTarget() {
		return target;
	}

	public S getInput() {
		return input;
	}

	public V getOutput() {
		return output;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(source.toString()).append(source);
		sb.append(" -> ").append(target).append(" i/o ").append(input).append("/").append(output);
		return sb.toString();
	}
}
