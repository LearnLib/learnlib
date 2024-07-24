package de.learnlib.query;

public abstract  class AdaptiveQuery<I,O> {

    public Boolean getIsFinished (){return false; } ;
    public I getInput(){return null;}

    public void processOutput( O out ) {}
}
