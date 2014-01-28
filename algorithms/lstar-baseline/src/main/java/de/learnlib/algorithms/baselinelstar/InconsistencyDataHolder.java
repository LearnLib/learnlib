/* Copyright (C) 2013-2014 TU Dortmund
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
