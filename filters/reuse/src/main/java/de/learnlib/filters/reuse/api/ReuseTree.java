package de.learnlib.filters.reuse.api;

import de.learnlib.logging.LearnLogger;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The {@link ReuseTree} holds all informations about already seen input/output
 * behavior from the system under test. It provides the possibility to
 * reference the internal system state.
 *
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 * @see SystemState
 */
public class ReuseTree {
	public static final String INPUT = "__PREFIX_INPUT__";
	public static final String OUTPUT = "__PREFIX_OUTPUT__";

	private ReuseNode root;

	private boolean useFailureOutputKnowledge = true;
	private boolean useModelInvariantSymbols = true;

	private final LearnLogger logger = LearnLogger.getLogger(ReuseTree.class.getName());

	private Set<String> readOnlyInputSymbols;
	private Set<String> failureOutputSymbols;

	/**
	 * Default constructor. Usage of domain knowledge about 'failure outputs' and
	 * 'model invariant input symbols' is enabled.
	 */
	public ReuseTree() {
		this.root = new ReuseNode("");
		this.readOnlyInputSymbols = new HashSet<>();
		this.failureOutputSymbols = new HashSet<>();
	}

	/**
	 * Adds a 'model invariant' input symbol to the set of 'read only' input
	 * symbols.
	 *
	 * @param input
	 * 		not allowed to be <code>null</code>.
	 * @see #useModelInvariantSymbols(boolean)
	 */
	public void addReadOnlyInputSymbol(String input) {
		this.logger.info("Added read only symbol: " + input);
		if (input == null) {
			throw new IllegalArgumentException("Input is not allowed to be null.");
		}
		this.readOnlyInputSymbols.add(input);
	}

	/**
	 * Adds a 'failure output' symbol to the set of failure output symbols.
	 *
	 * @param output
	 * 		not allowed to be <code>null</code>.
	 * @see #useFailureOutputKnowledge(boolean)
	 */
	public void addFailureOutputSymbol(String output) {
		this.logger.info("Added failure output: " + output);
		if (output == null) {
			throw new IllegalArgumentException("Output is not allowed to be null.");
		}
		this.failureOutputSymbols.add(output);
	}

	/**
	 * Whether to use domain knowledge about 'failure' outputs. If a membership
	 * query MQ is answered with some specific 'failure' output, the
	 * {@link SystemState} can be reused for the direct prefix of this query
	 * (indicated by a reflexive {@link ReuseEdge} in the {@link ReuseTree}),
	 * otherwise only if the MQ ends with a 'model invariant input symbol' or
	 * the new query is a continuation of this query.
	 *
	 * @param b
	 * @see #addFailureOutputSymbol(String)
	 */
	public void useFailureOutputKnowledge(boolean b) {
		this.useFailureOutputKnowledge = b;
	}

	/**
	 * Whether to use domain knowledge about 'model invariant' input symbols.
	 * Symbols are 'model invariant' if they never changes a state of the
	 * inferred model (independently from the output), i.e all transitions under
	 * the input are reflexive. This will be reflected by a reflexive
	 * {@link ReuseEdge} in the {@link ReuseTree}.
	 *
	 * @param b
	 * @see #addReadOnlyInputSymbol(String)
	 */
	public void useModelInvariantSymbols(boolean b) {
		this.useModelInvariantSymbols = b;
	}

	/**
	 * Returns the root {@link ReuseNode} of the {@link ReuseTree}.
	 *
	 * @return root The root of the tree, never <code>null</code>.
	 */
	public ReuseNode getRoot() {
		return this.root;
	}

	/**
	 * Returns the known output for the given query or <code>null</code> if not
	 * known.
	 *
	 * @param query
	 * 		Not allowed to be <code>null</code>.
	 * @return The output for <code>query</code> if already known from the
	 *         {@link ReuseTree} or <code>null</code> if unknown.
	 */
	public Word<Symbol> getOutput(final Word<Symbol> query) {
		if (query == null) {
			throw new IllegalArgumentException("Query is not allowed to be null.");
		}

		List<Symbol> output = new ArrayList<>(query.size());

		ReuseNode sink = getRoot();
		for (int i = 0; i <= query.size() - 1; i++) {
			ReuseNode node = sink.getTargetNodeForInput(getSymbol(query, i));
			ReuseEdge edge = sink.getEdgeWithInput(getSymbol(query, i));

			if (node == null) {
				return null;
			}

			output.add(new Symbol(edge.getOutput()));
			sink = node;
		}

		return Word.fromList(output);
	}

	/**
	 * Checks whether there exists a prefix of <code>query</code> (possibly the
	 * empty word) that has a {@link SystemState} that could be reused.
	 *
	 * @param query
	 * 		Not allowed to be <code>null</code>.
	 * @return Whether there exists a reuseable {@link SystemState} or not.
	 */
	public boolean hasReuseableSystemState(final Word<Symbol> query) {
		if (query == null) {
			throw new IllegalArgumentException("Query not allowed to be null.");
		}

		int length = 0;

		List<Symbol> prefixInput = new LinkedList<>();
		List<Symbol> prefixOutout = new LinkedList<>();

		ReuseNode sink = getRoot();
		ReuseNode lastState = null;
		if (sink.hasState()) {
			lastState = sink;
		}

		for (int i = 0; i <= query.size() - 1; i++) {
			ReuseNode node = sink.getTargetNodeForInput(getSymbol(query, i));
			ReuseEdge edge = sink.getEdgeWithInput(getSymbol(query, i));

			if (node == null) {
				break;
			}

			prefixInput.add(query.getSymbol(i));
			prefixOutout.add(new Symbol(edge.getOutput()));

			sink = node;
			if (sink.hasState()) {
				lastState = sink;
				length = i + 1;
			}
		}

		if (length == 0 && lastState == null) {
			return false;
		}

		return true;
	}

	/**
	 * Returns a reuseable {@link SystemState} or <code>null</code> if none such
	 * exists.
	 *
	 * @param query
	 * 		Not allowed to be <code>null</code>.
	 * @return
	 * @see #hasReuseableSystemState(Word)
	 */
	public SystemState getReuseableSystemState(Word<Symbol> query) {
		if (query == null) {
			throw new IllegalArgumentException("Query is not allowed to be null.");
		}

		int length = 0;

		List<Symbol> prefixInput = new LinkedList<>();
		List<Symbol> prefixOutout = new LinkedList<>();

		ReuseNode sink = getRoot();
		ReuseNode lastState = null;
		if (sink.hasState()) {
			lastState = sink;
		}

		for (int i = 0; i <= query.size() - 1; i++) {
			ReuseNode node = sink.getTargetNodeForInput(getSymbol(query, i));
			ReuseEdge edge = sink.getEdgeWithInput(getSymbol(query, i));

			if (node == null) {
				break;
			}

			prefixInput.add(query.getSymbol(i));
			prefixOutout.add(new Symbol(edge.getOutput()));

			sink = node;
			if (sink.hasState()) {
				lastState = sink;
				length = i + 1;
			}
		}

		if (length == 0 && lastState == null) {
			return null;
		}

		SystemState systemState = lastState.getSystemState();
		lastState.setSystemState(null);

		systemState.put(INPUT, Word.fromSymbols(prefixInput).prefix(length));
		systemState.put(OUTPUT, Word.fromSymbols(prefixOutout).prefix(length));

		return systemState;
	}

	/**
	 * Adds a {@link SystemState} the leaf that will be created in the
	 * {@link ReuseTree}. If there already exists a prefix of the corresponding
	 * input {@link Word} from the {@link SystemState}, this method will also
	 * check that be prefix consists of the same output symbols as the new one.
	 * If not a {@link RuntimeException} will be thrown.
	 *
	 * @param map
	 * 		Not allowed to be <code>null</code>.
	 */
	public void insert(SystemState map) {
		if (map == null) {
			throw new IllegalArgumentException("The systemstate is not allowed to be null.");
		}

		ReuseNode sink = getRoot();

		List<Symbol> suffixInput = new LinkedList<>();
		List<Symbol> suffixOutput = new LinkedList<>();

		Word<Symbol> input = (Word) map.get(INPUT);
		Word<Symbol> output = (Word) map.get(OUTPUT);

		for (int i = 0; i <= input.size() - 1; i++) {
			Symbol ii = input.getSymbol(i);
			Symbol oi = output.getSymbol(i);

			ReuseEdge e = sink.getEdgeWithInput(ii.toString().trim());
			if (e != null) {
				if (!e.getOutput().equals(oi.toString().trim())) {
					StringBuilder sb = new StringBuilder();
					sb.append("Error while inserting system state: \n:  ");
					sb.append("Input  = ").append(input).append("\n:  ");
					sb.append("Output = ").append(output).append("\n:  ");
					sb.append("inputsymbol ").append(ii);
					sb.append(" at position ").append(i);
					sb.append("\n:  has outputsymbol ").append(oi);
					sb.append("\n:  but the output should have been ");
					sb.append(e.getOutput());

					logger.warning(sb.toString());

					throw new RuntimeException(sb.toString());
				}
			}

			ReuseNode n = sink.getTargetNodeForInput(ii.toString().trim());
			if (n == null) {
				for (int j = i; j <= input.size() - 1; j++) {
					suffixInput.add(input.getSymbol(j));
					suffixOutput.add(output.getSymbol(j));
				}

				break;
			}
			sink = n;
		}

		for (int i = 0; i <= suffixInput.size() - 1; i++) {
			String in = getSymbol(Word.fromList(suffixInput), i);
			String out = getSymbol(Word.fromList(suffixOutput), i);
			ReuseNode rn;

			if (useFailureOutputKnowledge) {
				if (failureOutputSymbols.contains(out)) {
					rn = sink;
				}
				else if (useModelInvariantSymbols && readOnlyInputSymbols.contains(in)) {
					rn = sink;
				}
				else {
					rn = new ReuseNode(sink.getName() + " " + in);
				}
			}
			else {
				if (useModelInvariantSymbols
						&& readOnlyInputSymbols.contains(in)) {
					rn = sink;
				}
				else {
					rn = new ReuseNode(sink.getName() + " " + in);
				}
			}

			sink.addEdge(new ReuseEdge(sink, rn, in, out));
			sink = rn;
		}

		sink.setSystemState(map);
	}

	private String getSymbol(Word<Symbol> word, int index) {
		return word.getSymbol(index).toString().trim();
	}
}
