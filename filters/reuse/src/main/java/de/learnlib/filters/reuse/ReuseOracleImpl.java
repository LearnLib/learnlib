/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.filters.reuse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.filters.reuse.api.InjectableSystemStateRef;
import de.learnlib.filters.reuse.api.SystemStateRef;

/**
 * TODO Documentation!
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class ReuseOracleImpl<S extends SystemStateRef<?, I, O>, I extends InjectableSystemStateRef<S, I, O>, O>
		implements MealyMembershipOracle<I, O> {

	public enum NeededAction {
		RESET_NECCESSARY, ALREADY_KNOWN, PREPARE_PREFIX
	}

	/**
	 * The {@link ExecutableOracleImpl} to execute the query (or any suffix of a
	 * query). It is necessary that this {@link ExecutableOracleImpl} directly
	 * executes received queries on the SUT.
	 */
	private ExecutableOracleImpl<S, I, O> executableOracle;

	private ReuseTreeImpl<S, I, O> tree;

	private S systemState = null;

	private int answers = 0, full = 0, reuse = 0;

	/**
	 * Default constructor.
	 * 
	 * @param sut
	 *            The {@link ExecutableOracleImpl} to delegate queries to.
	 */
	public ReuseOracleImpl(ExecutableOracleImpl<S, I, O> sut) {
		this.executableOracle = sut;
		this.tree = new ReuseTreeImpl<>();
	}

	public int getAnswers() {
		return answers;
	}
	
	public int getFull() {
		return full;
	}
	
	public int getReuse() {
		return reuse;
	}
	
	/**
	 * @param query
	 * @return
	 */
	public NeededAction analyzeQuery(final Word<I> query) {
		if (tree.getOutput(query) != null) {
			return NeededAction.ALREADY_KNOWN;
		}
		if (!tree.hasReuseableSystemState(query)) {
			return NeededAction.RESET_NECCESSARY;
		} else {
			return NeededAction.PREPARE_PREFIX;
		}
	}

	public void setSystemstate(S systemState) {
		this.systemState = systemState;
	}

	public Word<O> answerQuery(final Word<I> query) {
		answers++;

		if (tree.getOutput(query) != null) {
			return tree.getOutput(query);
		}

		Word<O> knownOutput = tree.getOutput(query);
		if (knownOutput != null) {
			return knownOutput;
		}
		throw new RuntimeException(
				"Should not occour, analyzeQuery really returned ALREADY_KNOWN?");
	}

	public Word<O> executeFullQuery(final Word<I> query) {
		full++;

		this.systemState = this.executableOracle.reset();
		Word<O> result = executableOracle.processQuery(query);

		S state = executableOracle.getSystemState();
		state.setPrefixInput(query);
		state.setPrefixOutput(result);

		this.tree.insert(systemState);

		return result;
	}

	public Word<O> executeSuffixFromQuery(final Word<I> query) {
		reuse++;

		S reuseablePrefix = tree.getReuseableSystemState(query);
		executableOracle.setSystemState(reuseablePrefix);

		Word<I> inputQuery = reuseablePrefix.getPrefixInput();
		Word<O> outputQuery = reuseablePrefix.getPrefixOutput();

		int index = 0;
		List<O> prefixResult = new LinkedList<>();
		List<I> prefixInput = new LinkedList<>();
		for (int i = 0; i <= inputQuery.size() - 1; i++) {

			I a = inputQuery.getSymbol(i);
			I q = query.getSymbol(index);

			if (a.equals(q)) {
				prefixResult.add(outputQuery.getSymbol(i));
				prefixInput.add(query.getSymbol(index));

				index++;
			}
		}

		Word<I> suffix = query.suffix(query.size() - index);

		final Word<O> suffixResult = executableOracle.processQuery(suffix);
		final Word<O> output = Word.fromList(prefixResult).concat(suffixResult);

		S state = executableOracle.getSystemState();
		state.setPrefixInput(query);
		state.setPrefixOutput(output);

		this.tree.insert(executableOracle.getSystemState());

		return output;
	}

	@Override
	public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
		for (Query<I, Word<O>> query : queries) {
			Word<O> output = processQuery(query.getInput());
			query.answer(output.suffix(query.getSuffix().size()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	private synchronized final Word<O> processQuery(final Word<I> query) {
		Word<O> knownOutput = tree.getOutput(query);
		if (knownOutput != null) {
			answers++;
			return knownOutput;
		}

		S reuseablePrefix = tree.getReuseableSystemState(query);

		if (reuseablePrefix == null) {
			full++;
			
			executableOracle.reset();
			Word<O> result = executableOracle.processQuery(query);

			try {
				this.tree.insert(executableOracle.getSystemState());
			} catch (RuntimeException e) {
				e.printStackTrace();
			}

			return result;
		} else {
			reuse++;
			
			executableOracle.setSystemState(reuseablePrefix);

			Word<I> inputQuery = reuseablePrefix.getPrefixInput();
			Word<O> outputQuery = reuseablePrefix.getPrefixOutput();

			int index = 0;
			List<O> prefixResult = new LinkedList<>();
			List<I> prefixInput = new LinkedList<>();
			for (int i = 0; i <= inputQuery.size() - 1; i++) {

				I a = inputQuery.getSymbol(i);
				I q = query.getSymbol(index);

				if (a.equals(q)) {
					prefixResult.add(outputQuery.getSymbol(i));
					prefixInput.add(query.getSymbol(index));

					index++;
				}
			}

			Word<I> suffix = query.suffix(query.size() - index);

			// final Word suffixResult = executableOracle.processQuery(suffix);
			final Word<O> suffixResult = executableOracle
					.processQueryWithoutReset(suffix);
			final Word<O> output = Word.fromList(prefixResult).concat(
					suffixResult);

			this.tree.insert(executableOracle.getSystemState());

			return output;
		}
	}

	public ReuseTreeImpl<S, I, O> getReuseTree() {
		return this.tree;
	}

	public ExecutableOracleImpl<S, I, O> getExecutableOracle() {
		return this.executableOracle;
	}

}
