package com.linkmesh.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WorkerIdProvider implements InitializingBean, DisposableBean {

    @Value("${zookeeper.connect-string}")
    private String connectString;

    @Value("${zookeeper.connection-timeout-ms:15000}")
    private int connectionTimeoutMs;

    @Value("${zookeeper.worker-node-path:/linkmesh/workers}")
    private String workerNodePath;

    @Value("${snowflake.fallback-worker-id:0}")
    private long fallbackWorkerId;

    private CuratorFramework curator;
    private String acquiredNodePath;
    private long workerId;

    @Override
    public void afterPropertiesSet() {
        workerId = acquireWorkerId();
        log.info("WorkerID assigned: {}", workerId);
    }

    public long getWorkerId() {
        return workerId;
    }

    private long acquireWorkerId() {
        try {
            curator = CuratorFrameworkFactory.builder()
                    .connectString(connectString)
                    .sessionTimeoutMs(60000)
                    .connectionTimeoutMs(connectionTimeoutMs)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                    .build();
            curator.start();

            boolean connected = curator.blockUntilConnected(connectionTimeoutMs, TimeUnit.MILLISECONDS);
            if (!connected) {
                log.warn("ZooKeeper unreachable — falling back to workerId={}", fallbackWorkerId);
                closeCurator();
                return fallbackWorkerId;
            }

            if (curator.checkExists().forPath(workerNodePath) == null) {
                curator.create().creatingParentsIfNeeded().forPath(workerNodePath);
            }

            acquiredNodePath = curator.create()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(workerNodePath + "/worker-",
                            "active".getBytes(StandardCharsets.UTF_8));

            long sequence = parseSequence(acquiredNodePath);
            long assigned = sequence % 1024;
            log.info("ZooKeeper node: {} → workerId={}", acquiredNodePath, assigned);
            return assigned;

        } catch (Exception e) {
            log.error("ZooKeeper error: {} — falling back to workerId={}", e.getMessage(), fallbackWorkerId);
            closeCurator();
            return fallbackWorkerId;
        }
    }

    @Override
    public void destroy() {
        log.info("Releasing ZooKeeper node: {}", acquiredNodePath);
        closeCurator();
    }

    private long parseSequence(String path) {
        String[] parts = path.split("-");
        return Long.parseLong(parts[parts.length - 1]);
    }

    private void closeCurator() {
        if (curator != null) {
            try { curator.close(); } catch (Exception ignored) {}
        }
    }
}