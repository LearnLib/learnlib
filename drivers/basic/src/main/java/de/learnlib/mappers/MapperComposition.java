package de.learnlib.mappers;

import de.learnlib.api.SULException;

public class MapperComposition<AI, AO, ACI, CAO, CI, CO> implements Mapper<AI,AO,CI,CO> {
	
	private final Mapper<? super AI,? extends AO,ACI,CAO> mapper1;
	private final Mapper<? super ACI,? extends CAO,? extends CI,? super CO> mapper2;
	
	

	public MapperComposition(Mapper<? super AI,? extends AO,ACI,CAO> outerMapper,
			Mapper<? super ACI,? extends CAO,? extends CI,? super CO> innerMapper) {
		this.mapper1 = outerMapper;
		this.mapper2 = innerMapper;
	}

	@Override
	public void pre() {
		mapper1.pre();
		mapper2.pre();
	}

	@Override
	public void post() {
		mapper2.post();
		mapper1.post();
	}

	@Override
	public CI mapInput(AI abstractInput) {
		ACI aci = mapper1.mapInput(abstractInput);
		return mapper2.mapInput(aci);
	}

	@Override
	public AO mapOutput(CO concreteOutput) {
		CAO cao = mapper2.mapOutput(concreteOutput);
		return mapper1.mapOutput(cao);
	}

	@Override
	public MappedException<? extends AO> mapException(SULException exception) {
		MappedException<? extends CAO> mappedEx = mapper2
				.mapException(exception);

		AO thisStepOutput = mapper1.mapOutput(mappedEx.getThisStepOutput());
		switch (mappedEx.getPolicy()) {
		case PASS:
			return mapper1.mapException(mappedEx.getPassedException());
		case IGNORE_AND_CONTINUE:
			return MappedException.ignoreAndContinue(thisStepOutput);
		default: // case REPEAT_OUTPUT
			AO subsequentStepOutput = mapper1.mapOutput(mappedEx
					.getSubsequentStepsOutput());
			return MappedException.repeatOutput(thisStepOutput,
					subsequentStepOutput);
		}
	}

	
	
}
