package de.learnlib.algorithm.adt.Adaptive;

import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.adt.ADTResetNode;
import de.learnlib.algorithm.adt.adt.ADTSymbolNode;
import de.learnlib.algorithm.adt.automaton.ADTState;
import de.learnlib.algorithm.adt.automaton.ADTTransition;
import de.learnlib.algorithm.adt.util.ADTUtil;
import de.learnlib.query.AdaptiveQuery;
import net.automatalib.word.Word;

public class Adaptive_ADT_Query<I,O> extends AdaptiveQuery<I,O> {

    //the current ADTNode
    ADTNode<ADTState<I, O>, I, O> currentADTNode;

    //the transition to close. Will be closed by The Learner after the sifting is completed
    ADTTransition<I, O> transition;

    //the prefix of the target state with the one letter extension
    Word<I> longPrefix;

    //index of the current position in the longPrefix.
    int lpIndex;

    //flag for initialization.
    boolean needsInit;

    boolean needsReset;

    boolean isFinished;

    /*
        temporary ADTNode: if an ADT node does not have a successor for some output, the ADT node gets written to
        tempADTNode and the ouput gets written to tempOut.

        After sifting, the learner checks if any of these values are uninitialized for each query.
        If not, the corresponding ADT entries need to be updated.
     */

    ADTNode<ADTState<I, O>, I, O> tempADTNode;

    //outputSymbol of The Node That didn't Have A Successor for this OutputSymbol
    O tempOut;

    public Adaptive_ADT_Query(ADTTransition<I, O> transition, ADTNode<ADTState<I,O>,I,O> root ) {

        this.transition = transition;
        this.currentADTNode = root;
        this.longPrefix = transition.getSource().getAccessSequence().append(transition.getInput());
        this.lpIndex = 0;
        this.needsInit = true;
        this.needsReset = true;
        this.isFinished = false;
    }

    @Override
    public Boolean getIsFinished() {
        return this.isFinished;
    }

    @Override
    public I getInput() {
        return getInputFromCurrentADTNode();
    }

    @Override
    public void processOutput( O out ) {
        processOutputFromOracle( out );
    }


    /*

    INPUT FUNTION

        gets the next input symbol from the current ADTNode depending on the node type

        Symbol node: get the Symbol from the Symbol node.

        Reset node:
            current node reference stays on a reset node as long as the lp is not fully queried.
            the needsReset flag indicates to the Oracle if the SUL still needs a reset. If so, null is
            returned to and from the oracle.
            Once the lp is queried, the current pointer gets set to the next symbol node after
            the reset node

        Leaf node: return null and set the Finished flag
     */
    public I getInputFromCurrentADTNode() {

        /*
            at first, the transition output needs to be determined.
            the lp gets queried, including the transition input ( last symbol of lp ).

         */
        if( this.needsInit ) {
            return this.longPrefix.getSymbol(lpIndex);
        }

        //if the current node is a symbol node return the associated symbol.
        if(ADTUtil.isSymbolNode(currentADTNode)) {
            return ((ADTSymbolNode<ADTState<I,O>,I,O>)currentADTNode).getSymbol();

        } else if(ADTUtil.isResetNode(currentADTNode)) {

            if( this.needsReset ) {
                this.needsReset = false;
                return null;
            } else {
                I lpChar = this.longPrefix.getSymbol(lpIndex);
                lpIndex++;
                return lpChar;
            }
        } else if(ADTUtil.isLeafNode(currentADTNode)) {

            //tells the oracle to stop querying
            this.isFinished = true;
            return null;

        } else {
            throw new IllegalStateException("Tried to access invalid Node Type");
        }
    }


    /*

    OUTPUT FUNCTION

      determines the next ADTNode by picking the child node reachable by the edge labeled with the Output symbol
      provided by the oracle.

      the needsInit flag being true, signals that the query is still determining the transition output.
      If true, actual sifting has not started yet.

      - accSeq of transition source gets queried
      - transition in gets queried
      - transitionOut gets set to Oracle answer of transition input.

      Index meaning:
      send the symbol of the accSeq Word with index i, as long as i <= accSeq.len
      if i is equal to accSeq len, process the transition input.

      for the output function: Oracle outputs before the transition input can be discarded

    */

    public void processOutputFromOracle( O out ) {

        if( this.needsInit ) {

            if( this.lpIndex == this.transition.getSource().getAccessSequence().length()) {
                //query is returned the transition output symbol of the sul

                //TODO: This will be done by the learner eventually
                this.transition.setOutput(out);
                this.needsInit = false;
                this.lpIndex = 0;

            } else {
                /*
                query is returned null to signal that a character of the long prefix is processed properly.
                the output is not relevant and can be discarded
                 */

                lpIndex++;
            }

        } else if(ADTUtil.isLeafNode(currentADTNode)) {
            /*
                If the current node is a leaf node, the sifting operation successfully determined a transition
                target by reading the associated hypothesis state of the corresponding leaf node.

                There is no need anymore for a sifting operation.
             */
            throw new IllegalStateException("there are no successors after final nodes");

        } else if(ADTUtil.isSymbolNode(currentADTNode)) {

            //new state discovered while sifting
            if(currentADTNode.getChildren().get(out) == null) {

                /*
                save the ADT node and the output to let the learner fill the holes in the ADT.
                This is done this way, to avoid errors with parallel read / write operations.
                 */
                this.tempOut = out;
                this.tempADTNode = currentADTNode;
                this.isFinished = true;

            } else {

                //the actual sifting takes place here.
                this.currentADTNode = currentADTNode.getChildren().get(out);

            }
        } else if(ADTUtil.isResetNode(currentADTNode)) {

            /*

            First we have to check if we are still querying the long prefix.
            If so, don't set the current pointer to the next node.
            Reset nodes, by definition, only have one successor, so we can just call it after querying the long prefix.

             */

            if(this.lpIndex == this.longPrefix.length()  ) {
                this.currentADTNode = ((ADTResetNode)currentADTNode).getSuccessor();
                this.lpIndex = 0;
                this.needsReset = true;
            }
        } else {
            throw new IllegalStateException("Tried to access invalid Node Type");
        }
    }

    //returns true if a new ADT node was discovered while sifting and the related properties have been set
    public boolean needsPostProcessing() {
        return (this.tempOut != null && this.tempADTNode != null );
    }

    public ADTNode<ADTState<I,O>,I,O> getCurrentADTNode () {
        return this.currentADTNode;
    }

    public O getTempOut() {
        return this.tempOut;
    }

    public ADTTransition<I, O> getTransition() {
        return transition;
    }

    public Word<I> getLongPrefix() {
        return this.longPrefix;
    }
}


