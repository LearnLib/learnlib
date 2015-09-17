package de.learnlib.algorithms.ttt.base;

import java.util.HashMap;
import java.util.Iterator;

import net.automatalib.commons.util.array.RichArray;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.google.common.collect.AbstractIterator;

import de.learnlib.acex.AbstractCounterexample;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithms.ttt.dfa.TTTHypothesisDFA;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFA;
import de.learnlib.algorithms.ttt.dfa.TTTStateDFA;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public class EasyTTTPref<I> extends TTTLearnerDFA<I> {
	
	protected static class ExtDTNode<I> extends DTNode<I, Boolean> {
		
		
		private static class UnlabeledIterator<I> extends AbstractIterator<ExtDTNode<I>> {
			private ExtDTNode<I> curr;
			
			public UnlabeledIterator(ExtDTNode<I> curr) {
				this.curr = curr;
			}

			@Override
			protected ExtDTNode<I> computeNext() {
				curr = curr.nextUnlabeled;
				if (curr == null) {
					return endOfData();
				}
				return curr;
			}
		}
		
		private ExtDTNode<I> prevUnlabeled, nextUnlabeled;
		private int tempPrefix = -1;
		
		public void removeFromUnlabeledList() {
			prevUnlabeled.nextUnlabeled = nextUnlabeled;
			if (nextUnlabeled != null) {
				nextUnlabeled.prevUnlabeled = prevUnlabeled;
			}
		}
		
		@Override
		protected ExtDTNode<I> createChild(DTNode<I,Boolean> parent, Boolean parentIncoming) {
			return new ExtDTNode<>((ExtDTNode<I>) parent, parentIncoming);
		}
		
		public boolean hasUnlabeled() {
			return nextUnlabeled != null;
		}
		
		public void addUnlabeled(ExtDTNode<I> node) {
			node.nextUnlabeled = nextUnlabeled;
			if (nextUnlabeled != null) {
				nextUnlabeled.prevUnlabeled = node;
			}
			node.prevUnlabeled = this;
			this.nextUnlabeled = node;
		}
		
		public Iterator<ExtDTNode<I>> unlabeledIterator() {
			return new UnlabeledIterator<>(this);
		}
		
		public Iterable<ExtDTNode<I>> unlabeled() {
			return () -> unlabeledIterator();
		}
		
		public ExtDTNode() {
			super();
		}
		
		public ExtDTNode(ExtDTNode<I> parent, Boolean parentOut) {
			super(parent, parentOut);
		}
	}
	
	
	private final ExtDTNode<I> unlabeledList = new ExtDTNode<>();
	
	public EasyTTTPref(Alphabet<I> alphabet, MembershipOracle<I,Boolean> oracle,
			AcexAnalyzer analyzer) {
		super(alphabet, oracle, analyzer, new ExtDTNode<I>());
		
		split(dtree.getRoot(), Word.<I>epsilon(), false, true);
	}
	
	@Override
	protected TTTState<I, Boolean> makeTree(TTTTransition<I, Boolean> trans) {
		ExtDTNode<I> node = (ExtDTNode<I>) trans.getNonTreeTarget();
		if (node.tempPrefix != -1) {
			node.removeFromUnlabeledList();
		}
		return super.makeTree(trans);
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
		boolean refined = refineHypothesisSingle(ceQuery);
		if (!refined) {
			return false;
		}
		
		while (refineHypothesisSingle(ceQuery));
		
		return true;
	}
	
	private final class EasyTTTPrefAcex implements AbstractCounterexample<Boolean> {
		private final Word<I> ceWord;
		private final RichArray<ExtDTNode<I>> hypNodes;
		private final RichArray<ExtDTNode<I>> siftNodes;
		
		public EasyTTTPrefAcex(Word<I> ceWord) {
			this.ceWord = ceWord;
			this.hypNodes = new RichArray<>(ceWord.length() + 1);
			this.siftNodes = new RichArray<>(ceWord.length() + 1);
			
			update(ceWord.length());
		}
		
		@Override
		public int getLength() {
			return ceWord.length() + 1;
		}

		@Override
		public Boolean effect(int index) {
			ExtDTNode<I> hypNode = hypNodes.get(index);
			ExtDTNode<I> siftNode = siftNodes.get(index);
			if (siftNode == null) {
				siftNode = (ExtDTNode<I>) dtree.getRoot();
			}
			
			ExtDTNode<I> lca = (ExtDTNode<I>) dtree.leastCommonAncestor(hypNode, siftNode);
			Word<I> cePref = ceWord.prefix(index);
			while (lca == siftNode && siftNode != hypNode) {
				Boolean out = oracle.answerQuery(cePref, siftNode.getDiscriminator());
				siftNode = (ExtDTNode<I>) siftNode.getChild(out);
				lca = (ExtDTNode<I>) dtree.leastCommonAncestor(hypNode, siftNode);
			}
			siftNodes.set(index, siftNode);
			
			return siftNode == hypNode;
		}
		
		public void update(int len) {
			TTTStateDFA<I> curr = (TTTStateDFA<I>) hypothesis.getInitialState();
			hypNodes.set(0, (ExtDTNode<I>) curr.getDTLeaf());
			siftNodes.set(0, (ExtDTNode<I>) curr.getDTLeaf());
			
			boolean wasTree = true;
			for (int i = 0; i < len; i++) {
				I sym = ceWord.getSymbol(i);
				TTTTransition<I, Boolean> trans = hypothesis.getInternalTransition(curr, sym);
				curr = (TTTStateDFA<I>) trans.getTarget();
				
				hypNodes.set(i + 1, (ExtDTNode<I>) curr.getDTLeaf());
				if (wasTree) {
					siftNodes.set(i + 1, (ExtDTNode<I>) curr.getDTLeaf());
					if (!trans.isTree()) {
						wasTree = false;
					}
				}
				
			}
		}
		
		public ExtDTNode<I> getLCA(int index) {
			return (ExtDTNode<I>) dtree.leastCommonAncestor(hypNodes.get(index), siftNodes.get(index));
		}
		
		public ExtDTNode<I> getHypNode(int index) {
			return hypNodes.get(index);
		}
		

		@Override
		public boolean checkEffects(Boolean eff1, Boolean eff2) {
			return !eff1 || eff2;
		}
		
	}
	
	@Override
	protected boolean refineHypothesisSingle(DefaultQuery<I,Boolean> ceQuery) {
		if (((TTTHypothesisDFA<I>)hypothesis).computeSuffixOutput(ceQuery.getPrefix(), ceQuery.getSuffix()).equals(ceQuery.getOutput())) {
			return false;
		}
		
		Word<I> ceWord = ceQuery.getInput();
		int currReachInconsLength = ceWord.length();
		
		EasyTTTPrefAcex acex = new EasyTTTPrefAcex(ceWord);
		do {
			acex.update(currReachInconsLength);
			int breakpoint = analyzer.analyzeAbstractCounterexample(acex, 0, currReachInconsLength);
			ExtDTNode<I> toSplit = acex.getHypNode(breakpoint);
			TTTState<I,Boolean> splitState = toSplit.state;
			ExtDTNode<I> lca = acex.getLCA(breakpoint + 1);
			I sym = ceWord.getSymbol(breakpoint);
			Word<I> newDiscr = lca.getDiscriminator().prepend(sym);
			ExtDTNode<I> succHyp = acex.getHypNode(breakpoint + 1);
			boolean hypOut = lca.subtreeLabel(succHyp);
			openTransitions.insertAllIncoming(toSplit.getIncoming());
			DTNode<I,Boolean>[] children = toSplit.split(newDiscr, new HashMap<>(), hypOut, !hypOut);
			link(children[0], splitState);
			ExtDTNode<I> extUnlabeled = (ExtDTNode<I>) children[1];
			extUnlabeled.tempPrefix = currReachInconsLength;
			unlabeledList.addUnlabeled(extUnlabeled);
			closeTransitions();
			
			currReachInconsLength = findMinReachIncons();
		} while(currReachInconsLength != -1);
		
		return true;
	}
	
	private int findMinReachIncons() {
		int minLength = -1;
		for (ExtDTNode<I> n : unlabeledList.unlabeled()) {
			int len = n.tempPrefix;
			if (minLength == -1 || len < minLength) {
				minLength = len;
			}
		}
		return minLength;
	}
	
}
