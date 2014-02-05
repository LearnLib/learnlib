package de.learnlib.mappers.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import de.learnlib.api.SUL;
import de.learnlib.drivers.reflect.AbstractMethodInput;
import de.learnlib.drivers.reflect.AbstractMethodOutput;
import de.learnlib.mappers.ExecutableInputSUL;
import de.learnlib.mappers.Mappers;

import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.SimpleAlphabet;

public class NewSimplePOJOTestDriver {
	
	private final Alphabet<AbstractMethodInput> inputs = new SimpleAlphabet<>();
	private final SUL<AbstractMethodInput,AbstractMethodOutput> sul;
	
	public NewSimplePOJOTestDriver(Constructor<?> ctor, Object... ctorArgs) {
		NewSimplePOJOMapper mapper = new NewSimplePOJOMapper(ctor, ctorArgs);
		this.sul = Mappers.apply(mapper, new ExecutableInputSUL<>());
	}
	
	public AbstractMethodInput addInput(String name, Method m, Object ... params) {
        AbstractMethodInput i = new AbstractMethodInput(name, m, new HashMap<String, Integer>(), params);
        inputs.add(i);
        return i;
    }
	
	public SUL<AbstractMethodInput,AbstractMethodOutput> getSUL() {
		return sul;
	}

    /**
     * @return the inputs
     */
    public Alphabet<AbstractMethodInput> getInputs() {
        return this.inputs;
    }
    
}
