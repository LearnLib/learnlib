package de.learnlib.mappers;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import de.learnlib.mappers.Mapper.MappedException;

public class MappedSUL<AI, AO, CI, CO> implements SUL<AI, AO> {
	
	private final Mapper<? super AI,? extends AO,? extends CI,? super CO> mapper;
	private final SUL<? super CI,? extends CO> sul;
	
	private boolean inError = false;
	private AO repeatedErrorOutput = null;

	public MappedSUL(Mapper<? super AI,? extends AO,? extends CI,? super CO> mapper, SUL<? super CI,? extends CO> sul) {
		this.mapper = mapper;
		this.sul = sul;
	}

	@Override
	public void pre() {
		mapper.pre();
		sul.pre();
	}

	@Override
	public void post() {
		sul.post();
		mapper.post();
		this.inError = false;
		this.repeatedErrorOutput = null;
	}

	@Override
	public AO step(AI in) throws SULException {
		if(inError) {
			return repeatedErrorOutput;
		}
		
		CI concreteInput = mapper.mapInput(in);
		try {
			CO concreteOutput = sul.step(concreteInput);
			return mapper.mapOutput(concreteOutput);
		}
		catch(SULException ex) {
			MappedException<? extends AO> mappedEx = mapper.mapException(ex);
			switch(mappedEx.getPolicy()) {
			case PASS:
				throw mappedEx.getPassedException();
			case REPEAT_OUTPUT:
				this.inError = true;
				this.repeatedErrorOutput = mappedEx.getSubsequentStepsOutput();
			case IGNORE_AND_CONTINUE:
			}
			return mappedEx.getThisStepOutput();
		}
	}

}
