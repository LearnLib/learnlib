package de.learnlib.algorithm.adt.Adaptive;

import de.learnlib.sul.SUL;

public class SleepySUL<I,O> implements SUL<I,O>{

    private final long milis;
    private final int nanos;

    private final SUL<I,O> delegate;

    public SleepySUL(SUL<I, O> delegate, long milis, int nanos ) {
        this.milis = milis;
        this.nanos = nanos;

        this.delegate = delegate;
    }

    @Override
    public void pre() {

        try {
            Thread.sleep(milis,nanos);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        delegate.pre();
    }

    @Override
    public void post() {
        delegate.post();
    }

    @Override
    public O step(I i) {
        return  delegate.step( i );
    }

    @Override
    public boolean canFork() {
        return SUL.super.canFork();
    }

    @Override
    public SUL<I, O> fork() {
        return new SleepySUL<>(delegate.fork(),this.milis,this.nanos);
    }
}

