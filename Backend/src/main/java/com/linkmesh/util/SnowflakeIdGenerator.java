package com.linkmesh.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SnowflakeIdGenerator {

    // Custom epoch: 2024-01-01T00:00:00Z
    private static final long EPOCH            = 1_704_067_200_000L;

    private static final long WORKER_ID_BITS   = 10L;
    private static final long SEQUENCE_BITS    = 12L;

    private static final long MAX_WORKER_ID    = ~(-1L << WORKER_ID_BITS);  // 1023
    private static final long MAX_SEQUENCE     = ~(-1L << SEQUENCE_BITS);   // 4095

    private static final long WORKER_ID_SHIFT  = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT  = SEQUENCE_BITS + WORKER_ID_BITS;

    private final long workerId;
    private long sequence      = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                    "WorkerID must be between 0 and " + MAX_WORKER_ID + ", got: " + workerId);
        }
        this.workerId = workerId;
        log.info("LinkMesh SnowflakeIdGenerator started — workerId={}", workerId);
    }

    public synchronized long nextId() {
        long now = System.currentTimeMillis();

        if (now < lastTimestamp) {
            long drift = lastTimestamp - now;
            if (drift <= 5) {
                now = waitForNextMillis(lastTimestamp);
            } else {
                throw new IllegalStateException(
                        "Clock moved backwards by " + drift + "ms. Restart the instance.");
            }
        }

        if (now == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                now = waitForNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = now;

        return ((now - EPOCH)  << TIMESTAMP_SHIFT)
                | (workerId    << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitForNextMillis(long lastTs) {
        long ts = System.currentTimeMillis();
        while (ts <= lastTs) ts = System.currentTimeMillis();
        return ts;
    }

    public static long extractWorkerId(long id) {
        return (id >> WORKER_ID_SHIFT) & ~(-1L << WORKER_ID_BITS);
    }
}