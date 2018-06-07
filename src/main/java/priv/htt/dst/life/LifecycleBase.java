package priv.htt.dst.life;
import priv.htt.dst.exception.LifecycleException;

public abstract class LifecycleBase implements Lifecycle {

    private volatile LifecycleState state = LifecycleState.INIT;

    @Override
    public synchronized void start() throws LifecycleException {
        if (LifecycleState.STARTING.equals(state)
                || LifecycleState.STARTED.equals(state)) {
            return;
        }
        if (!LifecycleState.INIT.equals(state)) {
            stop();
        }
        startInternal();
        if (state.equals(LifecycleState.FAILED)) {
            stop();
        } else if (!state.equals(LifecycleState.STARTING)) {
            throw new LifecycleException("LifecycleBase.start, invalid transition, state: " + state);
        } else {
            setState(LifecycleState.STARTED);
        }
    }

    protected abstract void startInternal() throws LifecycleException;

    @Override
    public synchronized void stop() throws LifecycleException {
        if (LifecycleState.STOPPING.equals(state)
                || LifecycleState.STOPPED.equals(state)) {
            return;
        }
        if (state.equals(LifecycleState.INIT)) {
            state = LifecycleState.STOPPED;
            return;
        }
        if (!state.equals(LifecycleState.STARTED)
                && !state.equals(LifecycleState.FAILED)) {
            throw new LifecycleException("LifecycleBase.stop, invalid transition, state: " + state);
        }
        stopInternal();
        if (!state.equals(LifecycleState.STOPPING)
                && !state.equals(LifecycleState.FAILED)) {
            throw new LifecycleException("LifecycleBase.stop, invalid transition, state: " + state);
        }
        setState(LifecycleState.STOPPED);
    }

    protected abstract void stopInternal() throws LifecycleException;

    @Override
    public LifecycleState getState() {
        return state;
    }

    protected synchronized void setState(LifecycleState state) {
        this.state = state;
    }

}
