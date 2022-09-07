package de.learnlib.algorithms.ttt.moore;

import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import net.automatalib.words.Word;

public class TTTStateMoore<I, O> extends TTTState<I, Word<O>>{

    O output;

    public TTTStateMoore(int initialAlphabetSize, TTTTransition<I, Word<O>> parentTransition, int id) {
        super(initialAlphabetSize, parentTransition, id);
    }

    public O getOutput(){
        return output;
    }

    public void setOutput(O output){ this.output = output;}
}