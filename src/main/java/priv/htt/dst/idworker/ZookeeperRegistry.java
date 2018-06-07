package priv.htt.dst.idworker;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.htt.dst.exception.LifecycleException;
import priv.htt.dst.exception.ServiceException;
import priv.htt.dst.life.LifecycleBase;
import priv.htt.dst.life.LifecycleState;
import priv.htt.dst.util.JsonUtil;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ZookeeperRegistry extends LifecycleBase {

    private Logger LOG = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private CuratorFramework client;

    private String servers;

    private String namespace;

    private int retryIntervalMs = 5000;

    private int maxWaitTime = 60;

    public ZookeeperRegistry(String servers, String namespace) {
        this.servers = servers;
        this.namespace = namespace;
    }

    @Override
    public void startInternal() throws LifecycleException {
        try {
            setState(LifecycleState.STARTING);
            startClient();
        } catch (ServiceException e) {
            setState(LifecycleState.FAILED);
            throw new LifecycleException("LifecycleBase.start exception, Class: " + toString(), e);
        }
    }

    @Override
    public void stopInternal() throws LifecycleException {
        try {
            setState(LifecycleState.STOPPING);
            closeClient();
        } catch (Exception e) {
            setState(LifecycleState.FAILED);
            throw new LifecycleException("LifecycleBase.stop exception, Class: " + toString(), e);
        }
    }

    public CuratorFramework getClient() {
        return client;
    }

    public int getChildNum(String path) {
        try {
            Stat stat = getClient().checkExists().forPath(path);
            if (stat != null) {
                return stat.getNumChildren();
            }
        } catch (Exception e) {
            LOG.error("opt: getRegistedWorkersNum, exception.", e);
        }
        return 0;
    }

    public List<String> getChildrens(String path) {
        try {
            return getClient().getChildren().forPath(path);
        } catch (Exception e) {
            LOG.error("opt: getChildrens, exception.", e);
        }
        return Collections.emptyList();
    }

    public <T> T get(String path, Class<T> t) {
        try {
            byte[] bytes = getClient().getData().forPath(path);
            return JsonUtil.fromJson(new String(bytes), t);
        } catch (Exception e) {
            LOG.error("opt: getWorkerData, exception.", e);
        }
        return null;
    }

    public void set(String path, String data) {
        try {
            if (getClient().checkExists().forPath(path) == null) {
                getClient().create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path, data.getBytes(StandardCharsets.UTF_8));
            } else {
                getClient().setData().forPath(path, data.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            LOG.error("opt: getWorkerData, exception.", e);
        }
    }

    public void delete(String path) {
        try {
            getClient().delete().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            LOG.error("opt: deleteWorkerData, exception.", e);
        }
    }

    private synchronized void startClient() throws ServiceException {
        if (client != null) {
            return;
        }
        try {
            client = CuratorFrameworkFactory
                    .builder()
                    .connectString(servers)
                    .retryPolicy(new RetryForever(retryIntervalMs))
                    .namespace(namespace)
                    .build();
            client.start();
            if (client.blockUntilConnected(maxWaitTime, TimeUnit.SECONDS)) {
                return;
            }
            client.close();
        } catch (InterruptedException e) {
        }
        LOG.error("Connect to zk server failed, servers: {}.", servers);
        throw new ServiceException("Connect to zk server failed.");
    }

    private void closeClient() {
        if (client != null) {
            CloseableUtils.closeQuietly(client);
            client = null;
        }
    }

}