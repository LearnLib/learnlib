package de.learnlib.mappers;

import de.learnlib.api.SUL;

public abstract class Mappers {

	public static <AI,AO,ACI,CAO,CI,CO>
	Mapper<AI,AO,CI,CO> compose(Mapper<? super AI,? extends AO,ACI,CAO> outerMapper,
			Mapper<? super ACI,? extends CAO,? extends CI,? super CO> innerMapper) {
		return new MapperComposition<>(outerMapper, innerMapper);
	}
	
	public static <AI,AO,CI,CO>
	SUL<AI,AO> apply(Mapper<? super AI,? extends AO,CI,CO> mapper, SUL<? super CI,? extends CO> sul) {
		return new MappedSUL<>(mapper, sul);
	}
	
	
	private Mappers() {
		throw new AssertionError("Constructor should not be invoked");
	}

}
