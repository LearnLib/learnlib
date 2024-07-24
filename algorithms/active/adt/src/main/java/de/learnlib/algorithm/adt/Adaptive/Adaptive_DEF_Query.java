package de.learnlib.algorithm.adt.Adaptive;

import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.Query;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

public class Adaptive_DEF_Query<I,O> extends AdaptiveQuery<I,O> {

    private final Query<I, Word<O>> query;

    WordBuilder<O> builder =  new WordBuilder<>();

    Word<O> output;

    public boolean isFinished;

    int index = 0;

    public Adaptive_DEF_Query(Query<I, Word<O>> query) {
        this.query = query;
        this.isFinished = false;
    }


    @Override
    public Boolean getIsFinished() {
        return this.isFinished;
    }

    @Override
    public I getInput() {

        Word<I> fullInput = query.getInput();


        if( index > fullInput.length() ) {
            throw new IllegalStateException("index out of bounds for query");
        }

        if( fullInput.size() == 0 ) {
            return null;
        }
        I inputSymbol = fullInput.getSymbol(index);
        index ++;
        return inputSymbol;

    }

    @Override
    public void processOutput(O out) {

        if( out != null ) {
            builder.add( out );
        } else {
            query.answer(Word.epsilon());
            this.output = Word.epsilon();
            this.isFinished = true;
        }

        if( index == query.getInput().length() && index != 0 ) {

            Word<O> answer = builder.toWord();
            this.output = answer;
            query.answer(answer);

            this.isFinished = true;
        }
    }
}

