package de.learnlib.algorithms.dt;

import java.util.Map;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.MQUtil;

public class DTNode<I,O> {
	
	public static <I,O> DTNode<I,O> sift(DTNode<I,O> start, Word<I> word, MembershipOracle<I, O> oracle) {
		DTNode<I,O> curr = start;
		
		while(curr.discriminator != null) {
			O out = MQUtil.output(oracle, word.concat(curr.discriminator));
			DTNode<I,O> next = curr.children.get(out);
			if(next == null) {
				next = new DTNode<I,O>();
				curr.children.put(out, next);
			}
			curr = next;
		}
		
		return curr;
	}
	
	public static final int INNER_NODE = -1;
	
	private int stateId; 
	
	private Map<O,DTNode<I,O>> children;
	private Word<I> discriminator;

	public DTNode() {
		// TODO Auto-generated constructor stub
	}
	
	public DTNode<I,O> getChild(O output) {
		assert children != null;
		return children.get(output);
	}
	
	public void setChild(O output, DTNode<I,O> node) {
		assert children != null;
		children.put(output, node);
	}
	
	public int getStateId() {
		return stateId;
	}
	

}
