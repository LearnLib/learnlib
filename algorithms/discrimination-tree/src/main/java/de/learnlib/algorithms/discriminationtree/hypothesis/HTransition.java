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

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import de.learnlib.discriminationtree.DTNode;

public class HTransition<I, O, SP, TP> {
	
	// GENERAL PURPOSE FIELDS
	private final HState<I,O,SP,TP> source;
	private final I symbol;
	private TP property;
	
	// TREE EDGE FIELDS
	private HState<I,O,SP,TP> treeTgt;
	
	// NON-TREE EDGE FIELDS
	private DTNode<I,O,HState<I,O,SP,TP>> dt;
	

	public HTransition(HState<I,O,SP,TP> source, I symbol, DTNode<I,O,HState<I,O,SP,TP>> dtTgt) {
		this.source = source;
		this.symbol = symbol;
		this.treeTgt = null;
		this.dt = dtTgt;
	}
	
	public boolean isTree() {
		return (treeTgt != null);
	}
	
	public HState<I,O,SP,TP> getSource() {
		return source;
	}
	
	public I getSymbol() {
		return symbol;
	}
	
	public TP getProperty() {
		return property;
	}
	
	public void setProperty(TP property) {
		this.property = property;
	}
	
	public HState<I,O,SP,TP> getTreeTarget() {
		assert isTree();
		return treeTgt;
	}
	
	public DTNode<I,O,HState<I, O, SP, TP>> getDT() {
		assert !isTree();
		return dt;
	}
	
	public void setDT(DTNode<I,O,HState<I,O,SP,TP>> dtNode) {
		assert !isTree();
		this.dt = dtNode;
	}
	
	public void makeTree(HState<I,O,SP,TP> treeTgt) {
		if(this.treeTgt != null)
			throw new IllegalStateException("Cannot make transition [" + getAccessSequence() + "] a tree transition: already is");
		
		this.treeTgt = treeTgt;
		this.dt = null;
	}

	public Word<I> getAccessSequence() {
		WordBuilder<I> wb = new WordBuilder<I>(source.getDepth() + 1);
		source.appendAccessSequence(wb);
		wb.append(symbol);
		return wb.toWord();
	}

	public HState<I, O, SP, TP> nonTreeTarget() {
		assert !isTree();
		return dt.getData();
	}
	
	public HState<I, O, SP, TP> currentTarget() {
		if(treeTgt != null)
			return treeTgt;
		return dt.getData();
	}

}
