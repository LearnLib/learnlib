package de.learnlib.algorithm.adt.Adaptive;

import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.Query;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

public class PresetAdaptiveQuery<I,O> implements AdaptiveQuery<I,O> {

    private final Query<I, Word<O>> query;
    private final WordBuilder<O> builder;

    private int index;

    public PresetAdaptiveQuery(Query<I, Word<O>> query) {
        this.query = query;
        this.builder = new WordBuilder<>();
        this.index = 0;
    }

    @Override
    public I getInput() {
        final Word<I> input = query.getInput();

        if( index >= input.length() ) {
            throw new IllegalStateException("index out of bounds for query");
        }

        return input.getSymbol(index);
    }

    @Override
    public Response processOutput(O out) {
        builder.add(out);

        index++;

        if (index >= query.getInput().size()) {
            query.answer(builder.toWord());
            return Response.FINISHED;
        } else {
            return Response.SYMBOL;
        }
    }
}

