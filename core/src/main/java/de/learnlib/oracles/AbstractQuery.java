package de.learnlib.oracles;

import net.automatalib.words.Word;
import de.learnlib.api.Query;

public abstract class AbstractQuery<I, O> extends Query<I, O> {

	protected final Word<I> prefix;
	protected final Word<I> suffix;
	
	public AbstractQuery(Word<I> prefix, Word<I> suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	public AbstractQuery(Word<I> queryWord) {
		this(Word.<I>epsilon(), queryWord);
	}
	
	public AbstractQuery(Query<I,?> query) {
		this(query.getPrefix(), query.getSuffix());
	}

	@Override
	public Word<I> getPrefix() {
		return prefix;
	}

	@Override
	public Word<I> getSuffix() {
		return suffix;
	}

}
