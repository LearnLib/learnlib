package de.learnlib.filters.reuse;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.filters.reuse.api.ReuseTree;
import de.learnlib.filters.reuse.api.SystemState;
import de.learnlib.logging.LearnLogger;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class ReuseOracle implements MembershipOracle<Symbol, Word<Symbol>> {

	public enum NeededAction {
		RESET_NECCESSARY,
		ALREADY_KNOWN,
		PREPARE_PREFIX;
	}

	private static final LearnLogger LOGGER = LearnLogger.getLogger(ReuseOracle.class.getName());

	/**
	 * The {@link ExecutableOracle} to execute the query (or any suffix of a query). It is
	 * necessary that this {@link ExecutableOracle} directly executes received queries on
	 * the SUT.
	 */
	private ExecutableOracle executableOracle;

	private ReuseTree tree;
	private Set<Word<Symbol>> querySet = new HashSet<>();

	private SystemState systemState = null;

	@SuppressWarnings("unused")
	private int answers = 0, full = 0, reuse = 0;

	/**
	 * Default constructor.
	 *
	 * @param sut
	 * 		The {@link ExecutableOracle} to delegate queries to.
	 */
	public ReuseOracle(ExecutableOracle sut) {
		this.executableOracle = sut;
		this.tree = new ReuseTree();
	}

	/**
	 * @param query
	 * @return
	 */
	public NeededAction analyzeQuery(final Word<Symbol> query) {
		if (querySet.contains(query) || tree.getOutput(query) != null) {
			return NeededAction.ALREADY_KNOWN;
		}
		if (!tree.hasReuseableSystemState(query)) {
			return NeededAction.RESET_NECCESSARY;
		}
		else {
			return NeededAction.PREPARE_PREFIX;
		}
	}

	public void setSystemstate(SystemState systemState) {
		this.systemState = systemState;
	}

	public Word<Symbol> answerQuery(final Word<Symbol> query) {
		answers++;

		if (querySet.contains(query) || tree.getOutput(query) != null) {
			return tree.getOutput(query);
		}
		querySet.add(query);

		Word<Symbol> knownOutput = tree.getOutput(query);
		if (knownOutput != null) {
			return knownOutput;
		}
		throw new RuntimeException("Should not occour, analyzeQuery really returned ALREADY_KNOWN?");
	}

	public Word executeFullQuery(final Word<Symbol> query) {
		full++;

		this.systemState = this.executableOracle.reset();
		Word result = executableOracle.processQuery(query);

		SystemState state = executableOracle.getSystemState();
		state.put(ReuseTree.INPUT, query);
		state.put(ReuseTree.OUTPUT, result);

		this.tree.insert(systemState);

		return result;
	}

	public Word<Symbol> executeSuffixFromQuery(final Word<Symbol> query) {
		reuse++;

		SystemState reuseablePrefix = tree.getReuseableSystemState(query);
		executableOracle.setSystemState(reuseablePrefix);

		Word<Symbol> inputQuery = (Word) reuseablePrefix.get(ReuseTree.INPUT);
		Word<Symbol> outputQuery = (Word) reuseablePrefix.get(ReuseTree.OUTPUT);

		int index = 0;
		List<Symbol> prefixResult = new LinkedList<>();
		List<Symbol> prefixInput = new LinkedList<>();
		for (int i = 0; i <= inputQuery.size() - 1; i++) {

			String a = inputQuery.getSymbol(i).toString().trim();
			String q = query.getSymbol(index).toString().trim();

			if (a.equals(q)) {
				prefixResult.add(outputQuery.getSymbol(i));
				prefixInput.add(query.getSymbol(index));

				index++;
			}
		}

		Word suffix = query.suffix(query.size() - index);

		final Word<Symbol> suffixResult = executableOracle.processQuery(suffix);
		final Word<Symbol> output = Word.fromList(prefixResult).concat(suffixResult);

		SystemState state = executableOracle.getSystemState();
		state.put(ReuseTree.INPUT, query);
		state.put(ReuseTree.OUTPUT, output);

		this.tree.insert(executableOracle.getSystemState());

		return output;
	}

	@Override
	public void processQueries(Collection<? extends Query<Symbol, Word<Symbol>>> queries) {
		for (Query<Symbol, Word<Symbol>> query : queries) {
			Word<Symbol> output = processQuery(query.getInput());
			query.answer(output);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final Word<Symbol> processQuery(final Word<Symbol> query) {
		if (querySet.contains(query)) {
			Word<Symbol> output = tree.getOutput(query);

			if (output != null) {
				return tree.getOutput(query);
			}
			else {
				StringBuilder sb = new StringBuilder();
				sb.append("Output of tree was null, that should not happen. Query: ").append(query);
				sb.append(" Root is: ").append(tree.getRoot());
				LOGGER.warning(sb.toString());
			}
		}
		querySet.add(query);

		Word<Symbol> knownOutput = tree.getOutput(query);
		if (knownOutput != null) {
			return knownOutput;
		}

		SystemState reuseablePrefix = tree.getReuseableSystemState(query);

		if (reuseablePrefix == null) {
			executableOracle.reset();
			Word result = executableOracle.processQuery(query);

			try {
				this.tree.insert(executableOracle.getSystemState());
			}
			catch (RuntimeException e) {
				e.printStackTrace();
			}

			return result;
		}
		else {
			executableOracle.setSystemState(reuseablePrefix);

			Word<Symbol> inputQuery = (Word) reuseablePrefix.get(ReuseTree.INPUT);
			Word<Symbol> outputQuery = (Word) reuseablePrefix.get(ReuseTree.OUTPUT);

			int index = 0;
			List<Symbol> prefixResult = new LinkedList<>();
			List<Symbol> prefixInput = new LinkedList<>();
			for (int i = 0; i <= inputQuery.size() - 1; i++) {

				String a = inputQuery.getSymbol(i).toString().trim();
				String q = query.getSymbol(index).toString().trim();

				if (a.equals(q)) {
					prefixResult.add(outputQuery.getSymbol(i));
					prefixInput.add(query.getSymbol(index));

					index++;
				}
			}

			Word<Symbol> suffix = query.suffix(query.size() - index);

//			final Word suffixResult = executableOracle.processQuery(suffix);
			final Word suffixResult = executableOracle.processQueryWithoutReset(suffix);
			final Word output = Word.fromList(prefixResult).concat(suffixResult);

			this.tree.insert(executableOracle.getSystemState());

			return output;
		}
	}

	public ReuseTree getReuseTree() {
		return this.tree;
	}

	public ExecutableOracle getExecutableOracle() {
		return this.executableOracle;
	}
}
