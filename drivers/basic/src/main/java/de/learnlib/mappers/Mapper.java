package de.learnlib.mappers;

import de.learnlib.api.SULException;

public interface Mapper<AI,AO,CI,CO> {
	
	public static enum ExceptionMappingPolicy {
		PASS,
		IGNORE_AND_CONTINUE,
		REPEAT_OUTPUT
	}
	
	public static final class MappedException<AO> {
		
		public static <AO>
		MappedException<AO> ignoreAndContinue(AO output) {
			return new MappedException<>(ExceptionMappingPolicy.IGNORE_AND_CONTINUE, output, null);
		}
		
		public static <AO>
		MappedException<AO> repeatOutput(AO output, AO subsequentOutput) {
			return new MappedException<>(ExceptionMappingPolicy.REPEAT_OUTPUT, output, subsequentOutput);
		}
		
		public static <AO>
		MappedException<AO> repeatOutput(AO output) {
			return repeatOutput(output, output);
		}
		
		public static <AO>
		MappedException<AO> pass(SULException exception) {
			return new MappedException<>(exception);
		}
		
		private final ExceptionMappingPolicy policy;
		private final AO thisStepOutput;
		private final AO subsequentStepsOutput;
		private final SULException passedException;
		
		private MappedException(ExceptionMappingPolicy policy, AO thisStepOutput, AO subsequentStepsOutput) {
			this.policy = policy;
			this.thisStepOutput = thisStepOutput;
			this.subsequentStepsOutput = subsequentStepsOutput;
			this.passedException = null;
		}
		
		private MappedException(SULException passedException) {
			this.policy = ExceptionMappingPolicy.PASS;
			this.thisStepOutput = null;
			this.subsequentStepsOutput = null;
			this.passedException = passedException;
		}
		
		
		public ExceptionMappingPolicy getPolicy() {
			return policy;
		}
		
		public AO getThisStepOutput() {
			return thisStepOutput;
		}
		
		public AO getSubsequentStepsOutput() {
			return subsequentStepsOutput;
		}
		
		public SULException getPassedException() {
			return passedException;
		}
	}

	public void pre();
	
	public void post();
	
	public CI mapInput(AI abstractInput);
	public AO mapOutput(CO concreteOutput);
	
	/**
	 * Maps an exception to an abstract output symbol.
	 * 
	 * @param exception the exception that was thrown
	 * @return the concrete output symbol the exception was mapped to, if applicable
	 */
	public MappedException<? extends AO> mapException(SULException exception);
	
}
