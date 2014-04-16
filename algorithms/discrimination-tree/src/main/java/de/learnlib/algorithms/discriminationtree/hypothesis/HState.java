/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.algorithms.discriminationtree.hypothesis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import de.learnlib.discriminationtree.DTNode;

public class HState<I,O,SP,TP> {

	private final HTransition<I,O,SP,TP> treeIncoming;
	private final int id;
	private final int depth;
	
	private DTNode<I,O,HState<I,O,SP,TP>> dtLeaf;
	
	private SP property;
	
	private final HTransition<I,O,SP,TP>[] transitions;
	
	private final List<HTransition<I,O,SP,TP>> nonTreeIncoming = new ArrayList<>();
	
	public HState(int alphabetSize) {	
		this(alphabetSize, 0, null);
	}
	
	@SuppressWarnings("unchecked")
	public HState(int alphabetSize, int id, HTransition<I,O,SP,TP> treeIncoming) {
		this.id = id;
		this.treeIncoming = treeIncoming;
		this.depth = (treeIncoming == null) ? 0 : treeIncoming.getSource().depth + 1;
		this.transitions = new HTransition[alphabetSize];
	}
	
	
	public DTNode<I,O,HState<I,O,SP,TP>> getDTLeaf() {
		return dtLeaf;
	}
	
	public void setDTLeaf(DTNode<I,O,HState<I,O,SP,TP>> dtLeaf) {
		this.dtLeaf = dtLeaf;
	}
	
	public HTransition<I,O,SP,TP> getTreeIncoming() {
		return treeIncoming;
	}
	
	public void appendAccessSequence(List<? super I> symList) {
		if(treeIncoming == null)
			return;
		treeIncoming.getSource().appendAccessSequence(symList);
		symList.add(treeIncoming.getSymbol());
	}
	
	public Word<I> getAccessSequence() {
		if(treeIncoming == null)
			return Word.epsilon();
		WordBuilder<I> wb = new WordBuilder<>(depth);
		appendAccessSequence(wb);
		return wb.toWord();
	}
	
	
	public SP getProperty() {
		return property;
	}
	
	public void setProperty(SP property) {
		this.property = property;
	}
	
	public int getId() {
		return id;
	}
	
	public HTransition<I,O,SP,TP> getTransition(int transIdx) {
		return transitions[transIdx];
	}
	
	public void setTransition(int transIdx, HTransition<I,O,SP,TP> transition) {
		transitions[transIdx] = transition;
	}
	
	public Collection<HTransition<I,O,SP,TP>> getOutgoingTransitions() {
		return Collections.unmodifiableList(Arrays.asList(transitions));
	}

	public int getDepth() {
		return depth;
	}
	
	public void addNonTreeIncoming(HTransition<I,O,SP,TP> trans) {
		nonTreeIncoming.add(trans);
	}
	
	public void fetchNonTreeIncoming(Collection<? super HTransition<I,O,SP,TP>> target) {
		target.addAll(nonTreeIncoming);
		nonTreeIncoming.clear();
	}
	
	@Override
	public String toString() {
		return "q" + id;
	}
}
