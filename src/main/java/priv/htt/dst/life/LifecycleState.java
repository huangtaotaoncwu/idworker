package priv.htt.dst.life;

public enum LifecycleState {

    INIT(Lifecycle.INIT),

    STARTING(Lifecycle.STARTING),

    STARTED(Lifecycle.STARTED),

    STOPPING(Lifecycle.STOPPING),

    STOPPED(Lifecycle.STOPPED),

    FAILED(Lifecycle.FAILED),;

    private String state;

    LifecycleState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
