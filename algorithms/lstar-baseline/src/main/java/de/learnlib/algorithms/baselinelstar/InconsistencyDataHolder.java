/* Copyright (C) 2013-2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithms.baselinelstar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.features.observationtable.ObservationTable.Inconsistency;
import de.learnlib.algorithms.features.observationtable.ObservationTable.Row;

import net.automatalib.words.Word;

@ParametersAreNonnullByDefault
public class InconsistencyDataHolder<I> implements Inconsistency<I,Boolean> {

	@Nonnull
	private final ObservationTableRow<I> firstRow;

	@Nonnull
	private final ObservationTableRow<I> secondRow;

	@Nonnull
	private final I differingSymbol;

	public InconsistencyDataHolder(ObservationTableRow<I> firstRow,
			ObservationTableRow<I> secondRow,
			@Nullable I differingSymbol) {
		this.firstRow = firstRow;
		this.secondRow = secondRow;
		this.differingSymbol = differingSymbol;
	}

	@Nonnull
	public Word<I> getFirstState() {
		return firstRow.getLabel();
	}

	@Nonnull
	public Word<I> getSecondState() {
		return secondRow.getLabel();
	}

	@Nonnull
	public I getDifferingSymbol() {
		return differingSymbol;
	}

	@Override
	public Row<I, Boolean> getFirstRow() {
		return secondRow;
	}

	@Override
	public Row<I, Boolean> getSecondRow() {
		return firstRow;
	}

	@Override
	public I getSymbol() {
		return differingSymbol;
	}
}
