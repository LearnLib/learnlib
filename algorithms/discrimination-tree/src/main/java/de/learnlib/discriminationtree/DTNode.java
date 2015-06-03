/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.discriminationtree;

import java.util.Collection;
import java.util.Map;

import net.automatalib.words.Word;

public abstract class DTNode<I, O, D> {
	
	public static class SplitResult<I,O,D> {
		public final DTNode<I,O,D> nodeOld;
		public final DTNode<I,O,D> nodeNew;
		
		public SplitResult(DTNode<I, O, D> nodeOld, DTNode<I, O, D> nodeNew) {
			this.nodeOld = nodeOld;
			this.nodeNew = nodeNew;
		}
	}

	protected final DTNode<I,O,D> parent;
	protected final O parentOutcome;
	protected final int depth;
	
	protected Map<O,DTNode<I,O,D>> children = null;
	protected Word<I> discriminator;
	protected D data;

	public DTNode(D data) {
		this(null, null, data);
	}
	
	protected DTNode(DTNode<I,O,D> parent, O parentOutcome, D data) {
		this.parent = parent;
		this.parentOutcome = parentOutcome;
		this.depth = (parent != null) ? parent.depth + 1 : 0;
		this.data = data;
	}
	
	public boolean isRoot() {
		return parent == null;
	}
	
	public DTNode<I,O,D> getParent() {
		return parent;
	}
	
	public O getParentOutcome() {
		return parentOutcome;
	}
	
	public Word<I> getDiscriminator() {
		return discriminator;
	}
	
	public DTNode<I,O,D> getChild(O out) {
		return children.get(out);
	}
	
	protected DTNode<I,O,D> addChild(O outcome, D data) {
		DTNode<I,O,D> child = createChild(outcome, data);
		children.put(outcome, child);
		return child;
	}
	
	public SplitResult<I,O,D> split(Word<I> discriminator, O oldOut, O newOut, D newData) {
		this.children = createChildMap();
		DTNode<I,O,D> nodeOld = addChild(oldOut, this.data);
		this.data = null;
		DTNode<I,O,D> nodeNew = addChild(newOut, newData);
		this.discriminator = discriminator;
		
		return new SplitResult<>(nodeOld, nodeNew);
	}
	
	public DTNode<I,O,D> child(O out) {
		return child(out, null);
	}
	
	public DTNode<I,O,D> child(O out, D defaultData) {
		assert !isLeaf();
		
		DTNode<I,O,D> result = getChild(out);
		if(result == null) {
			result = addChild(out, defaultData);
		}
		return result;
	}
	
	public boolean isLeaf() {
		return (children == null);
	}
	
	public Collection<Map.Entry<O,DTNode<I,O,D>>> getChildEntries() {
		return children.entrySet();
	}
	
	public D getData() {
		assert isLeaf();
		return data;
	}
	
	public void setData(D data) {
		assert isLeaf();
		this.data = data;
	}
	
	protected abstract Map<O,DTNode<I,O,D>> createChildMap();
	
	protected abstract DTNode<I,O,D> createChild(O outcome, D data);
	
}
