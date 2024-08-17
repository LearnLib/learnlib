package de.learnlib.algorithm.adt.learner;

import java.util.ArrayDeque;
import java.util.Deque;

import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.adt.ADTResetNode;
import de.learnlib.algorithm.adt.automaton.ADTState;
import de.learnlib.algorithm.adt.util.ADTUtil;
import de.learnlib.query.AdaptiveQuery;
import net.automatalib.word.Word;

class ADSAmbiguityQuery<I, O> implements AdaptiveQuery<I, O> {

    private ADTNode<ADTState<I, O>, I, O> currentADTNode;

    /**
     * The index of the access sequence of the transition. If equal to the length of the access sequence the actual
     * transition input symbol is the current symbol. If larger than the length of the access sequence, the symbol
     * should be fetched from the ADT nodes.
     */
    private int asIndex;
    private final Word<I> accessSequence;
    private final Deque<I> oneShotPrefix;

    private boolean inOneShot;
    private ADTNode<ADTState<I, O>, I, O> tempADTNode;
    private O tempOut;

    ADSAmbiguityQuery(Word<I> accessSequence, Word<I> oneShotPrefix, ADTNode<ADTState<I, O>, I, O> root) {
        this.currentADTNode = root;
        this.accessSequence = accessSequence;
        this.oneShotPrefix = new ArrayDeque<>(oneShotPrefix.asList());
        this.inOneShot = false;
        this.asIndex = 0;
    }

    @Override
    public I getInput() {
        if (this.asIndex < this.accessSequence.length()) {
            return this.accessSequence.getSymbol(this.asIndex);
        } else {
            this.inOneShot = !this.oneShotPrefix.isEmpty();
            if (this.inOneShot) {
                return oneShotPrefix.poll();
            } else {
                return this.currentADTNode.getSymbol();
            }
        }
    }

    @Override
    public Response processOutput(O out) {

        if (this.asIndex < this.accessSequence.length()) {

            // if the ADT only consists of a leaf, we just set the transition output
            if (ADTUtil.isLeafNode(this.currentADTNode)) {
                return Response.FINISHED;
            }

            asIndex++;
            return Response.SYMBOL;
        } else if (this.inOneShot) {
            return Response.SYMBOL;
        } else {
            final ADTNode<ADTState<I, O>, I, O> succ = currentADTNode.getChildren().get(out);

            if (succ == null) {
                this.tempOut = out;
                this.tempADTNode = currentADTNode;
                return Response.FINISHED;
            } else if (ADTUtil.isResetNode(succ)) {
                final ADTResetNode<ADTState<I, O>, I, O> asResetNode = (ADTResetNode<ADTState<I, O>, I, O>) succ;
                this.currentADTNode = asResetNode.getSuccessor();
                this.asIndex = 0;
                return Response.RESET;
            } else if (ADTUtil.isSymbolNode(succ)) {
                this.currentADTNode = succ;
                return Response.SYMBOL;
            } else {
                this.currentADTNode = succ;
                return Response.FINISHED;
            }
        }
    }

    boolean needsPostProcessing() {
        return (this.tempOut != null && this.tempADTNode != null);
    }

    ADTNode<ADTState<I, O>, I, O> getCurrentADTNode() {
        return this.currentADTNode;
    }

    O getTempOut() {
        return this.tempOut;
    }
}


