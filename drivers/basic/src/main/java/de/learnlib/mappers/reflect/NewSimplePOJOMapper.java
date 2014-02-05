package de.learnlib.mappers.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import de.learnlib.api.SULException;
import de.learnlib.drivers.reflect.AbstractMethodInput;
import de.learnlib.drivers.reflect.AbstractMethodOutput;
import de.learnlib.drivers.reflect.ConcreteMethodInput;
import de.learnlib.drivers.reflect.Error;
import de.learnlib.drivers.reflect.ReturnValue;
import de.learnlib.drivers.reflect.Unobserved;
import de.learnlib.mappers.Mapper;

public class NewSimplePOJOMapper implements Mapper<AbstractMethodInput,AbstractMethodOutput,ConcreteMethodInput,Object> {

	private final Constructor<?> initMethod;
    private final Object[] initParams;
                
    private Object _this;

    public NewSimplePOJOMapper(Constructor<?> initMethod, Object[] initParams) {
        this.initMethod = initMethod;
        this.initParams = initParams;
    }
    
    @Override
    public void pre() {
        try {
            _this = initMethod.newInstance(initParams);
        } catch (InstantiationException | IllegalAccessException | 
                IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void post() {
        _this = null;
    }

    @Override
    public ConcreteMethodInput mapInput(AbstractMethodInput i) {
        Map<String, Object> params = new HashMap<>();
        
        return new ConcreteMethodInput(i, params, _this);
    }

    @Override
    public AbstractMethodOutput mapOutput(Object o) {    
        return new ReturnValue(o);
    }

    @Override
    public MappedException<AbstractMethodOutput> mapException(SULException e) {
        return MappedException.repeatOutput(new Error((e.getCause() == null) ? e : e.getCause()), Unobserved.INSTANCE);
    }
    
    
    public static void doNothing() {
    }

}
