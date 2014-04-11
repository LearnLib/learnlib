package de.learnlib.algorithms.discriminationtree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.automatalib.words.Word;

public class DTNode<I, O, D> {
	
	public static class SplitResult<I,O,D> {
		public final DTNode<I,O,D> nodeOld;
		public final DTNode<I,O,D> nodeNew;
		
		public SplitResult(DTNode<I, O, D> nodeOld, DTNode<I, O, D> nodeNew) {
			this.nodeOld = nodeOld;
			this.nodeNew = nodeNew;
		}
	}

	private Word<I> discriminator;
	private Map<O,DTNode<I,O,D>> children;
	
	private D data;

	public DTNode(D data) {
		this.data = data;
	}
	
	public SplitResult<I,O,D> split(Word<I> discriminator, O oldOut, O newOut, D newData) {
		this.children = new HashMap<>();
		DTNode<I,O,D> nodeOld = createNode(this.data);
		children.put(oldOut, nodeOld);
		this.data = null;
		DTNode<I,O,D> nodeNew = createNode(newData);
		children.put(newOut, nodeNew);
		this.discriminator = discriminator;
		
		return new SplitResult<>(nodeOld, nodeNew);
	}
	
	
	public Word<I> getDiscriminator() {
		return discriminator;
	}
	
	public DTNode<I,O,D> getChild(O out) {
		return children.get(out);
	}
	
	public DTNode<I,O,D> child(O out) {
		DTNode<I,O,D> result = children.get(out);
		if(result == null) {
			result = createNode(null);
			children.put(out, result);
		}
		return result;
	}
	
	public boolean isLeaf() {
		return (children == null);
	}
	
	public Collection<Map.Entry<O,DTNode<I,O,D>>> getChildEntries() {
		assert !isLeaf();
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
	
	protected DTNode<I,O,D> createNode(D data) {
		return new DTNode<>(data);
	}
}
