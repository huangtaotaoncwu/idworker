package priv.htt.dst.idworker;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.htt.dst.bean.WorkerNode;
import priv.htt.dst.exception.LifecycleException;
import priv.htt.dst.exception.LockTimeoutException;
import priv.htt.dst.exception.ServiceException;
import priv.htt.dst.life.LifecycleBase;
import priv.htt.dst.life.LifecycleState;
import priv.htt.dst.util.IpUtil;
import priv.htt.dst.util.JsonUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ZookeeperWokerIdRegister extends LifecycleBase {

    private Logger LOG = LoggerFactory.getLogger(ZookeeperWokerIdRegister.class);

    private ZookeeperRegistry registry;

    private static final String WORKER_NODE = "/worker";

    private static final String LOCK_NODE = "/lock";

    private static final int LOCK_TIMEOUT = 10;

    private long maxWorkerId;

    private Long workerId;

    public ZookeeperWokerIdRegister(ZookeeperRegistry registry, long maxWorkerId) {
        this.registry = registry;
        this.maxWorkerId = maxWorkerId;
    }

    @Override
    protected void startInternal() throws LifecycleException {
        try {
            setState(LifecycleState.STARTING);
            registry.start();
        } catch (Exception e) {
            setState(LifecycleState.FAILED);
            throw new LifecycleException("LifecycleBase.start exception, Class: " + toString(), e);
        }
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        try {
            setState(LifecycleState.STOPPING);
            unregister();
        } catch (Exception e) {
            setState(LifecycleState.FAILED);
            throw new LifecycleException("LifecycleBase.stop exception, Class: " + toString(), e);
        }
        registry.stop();
    }

    public Long getWorkerId() {
        return this.workerId;
    }

    public void addListener(ConnectionStateListener listener) {
        this.registry.getClient().getConnectionStateListenable().addListener(listener);
    }

    public long register() throws ServiceException {
        int registedWorkersNum = registry.getChildNum(WORKER_NODE);
        if (registedWorkersNum >= maxWorkerId) {
            throw new ServiceException("Error, worker id already exhaust.");
        }
        InterProcessMutex lock = new InterProcessMutex(getClient(), LOCK_NODE);
        try {
            if (!lock.acquire(LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                throw new LockTimeoutException(WORKER_NODE);
            }
            List<String> childrens = registry.getChildrens(WORKER_NODE);
            for (long id = 0; id < maxWorkerId; id++) {
                WorkerNode workerNode = createWorkerNode(getSessionId(), id);
                String workerPath = WORKER_NODE + "/" + id;
                if (childrens.contains(String.valueOf(id))) {
                    WorkerNode workerData = registry.get(workerPath, WorkerNode.class);
                    if ((workerData != null) && workerData.equals(workerNode)) {
                        workerData.setUpdateTime(System.currentTimeMillis());
                        registry.set(workerPath, JsonUtil.toJson(workerData));
                        this.workerId = id;
                        return id;
                    }
                } else {
                    registry.set(workerPath, JsonUtil.toJson(workerNode));
                    this.workerId = id;
                    return id;
                }
            }
            throw new ServiceException("Error, register worker id failed.");
        } catch (Exception e) {
            String errorMsg = "Register worker id exception.";
            LOG.error(errorMsg, e);
            throw new ServiceException(errorMsg);
        } finally {
            try {
                lock.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void unregister() {
        if ((getClient() != null) && (workerId != null)) {
            registry.delete(WORKER_NODE + "/" + workerId);
        }
    }

    private CuratorFramework getClient() {
        return registry.getClient();
    }

    private long getSessionId() throws Exception {
        return getClient().getZookeeperClient().getZooKeeper().getSessionId();
    }

    private WorkerNode createWorkerNode(long sessionId, long workerId) {
        WorkerNode workerNode = new WorkerNode();
        workerNode.setSessionId(sessionId);
        workerNode.setWorkerId(workerId);
        workerNode.setIp(IpUtil.getLocalIp());
        workerNode.setCreateTime(System.currentTimeMillis());
        workerNode.setUpdateTime(System.currentTimeMillis());
        return workerNode;
    }

}