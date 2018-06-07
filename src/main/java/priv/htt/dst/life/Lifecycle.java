package priv.htt.dst.life;

import priv.htt.dst.exception.LifecycleException;

public interface Lifecycle {

    public static final String INIT = "init";

    public static final String STARTING = "starting";

    public static final String STARTED = "started";

    public static final String STOPPING = "stopping";

    public static final String STOPPED = "stopped";

    public static final String FAILED = "failed";

    void start() throws LifecycleException;

    void stop() throws LifecycleException;

    LifecycleState getState();

}
