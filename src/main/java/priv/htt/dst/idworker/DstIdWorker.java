package priv.htt.dst.idworker;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.htt.dst.exception.LifecycleException;
import priv.htt.dst.life.LifecycleBase;
import priv.htt.dst.life.LifecycleState;

public class DstIdWorker extends LifecycleBase implements IdWorker {

    private Logger LOG = LoggerFactory.getLogger(DstIdWorker.class);

    private IdWorker idWorker;

    private ZookeeperWokerIdRegister register;

    public DstIdWorker(String servers, String namespace) {
        this.register = new ZookeeperWokerIdRegister(
                new ZookeeperRegistry(servers, namespace),
                Snowflake.getMaxWorkerId());
    }

    @Override
    protected void startInternal() throws LifecycleException {
        try {
            setState(LifecycleState.STARTING);
            register.start();
            startListener();
            idWorker = Snowflake.getInstance(register.register());
        } catch (Exception e) {
            setState(LifecycleState.FAILED);
            throw new LifecycleException("LifecycleBase.start exception, Class: " + toString(), e);
        }
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        try {
            setState(LifecycleState.STOPPING);
            if (register != null) {
                register.stop();
            }
        } catch (Exception e) {
            setState(LifecycleState.FAILED);
            throw new LifecycleException("LifecycleBase.stop exception, Class: " + toString(), e);
        }
    }

    private void startListener() {
        register.addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                switch (newState) {
                    case LOST:
                        restart();
                        break;
                    case SUSPENDED:
                        restart();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void restart() {
        try {
            this.stop();
            this.start();
        } catch (LifecycleException e) {
            LOG.error("LifecycleBase.start exception.", e);
        }
    }

    private boolean isWorking() {
        return (register != null) && (getState() == LifecycleState.STARTED);
    }

    @Override
    public long nextId() {
        if (isWorking()) {
            return idWorker.nextId();
        }
        throw new IllegalStateException("IdWorker isn't working.");
    }

    @Override
    public long[] nextIds(int size) {
        if (isWorking()) {
            return idWorker.nextIds(size);
        }
        throw new IllegalStateException("IdWorker isn't working.");
    }

}
