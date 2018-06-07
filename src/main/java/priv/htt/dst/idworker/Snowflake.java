package priv.htt.dst.idworker;

/**
 * Snowflake:
 *
 * long型：64位，0-时间戳-机器位-序列号
 *
 * <br>最高位，符号位，0
 * <br>41位，时间位，时间戳=当前时间戳 - twepoch
 * <br>10位，机器位，最大支持1024台机器
 * <br>12位，毫秒序列号，最大支持4096/ms
 */
public class Snowflake implements IdWorker {

    private final long workerId;

    // 2018-01-01
    private final static long twepoch = 1514736000000L;

    private long sequence = 0L;

    // 机器数量，10位，最大支持1024台机器
    private final static long workerIdBits = 10L;

    // 0 ~ 1023
    private final static long maxWorkerId = -1L ^ -1L << workerIdBits;

    // 毫秒序列号，12位，最大支持4096/ms，即4096000/s
    private final static long sequenceBits = 12L;

    private final static long workerIdShift = sequenceBits;

    private final static long timestampLeftShift = sequenceBits + workerIdBits;

    private final static long sequenceMask = -1L ^ -1L << sequenceBits;

    private long lastTimestamp = -1L;

    private static final int MAX_SIZE = 100000;

    private Snowflake(final long workerId) {
        if ((workerId < 0) || (workerId > maxWorkerId)) {
            throw new IllegalArgumentException(String.format(
                    "workerId is illegal, workerId:[0-%d]", maxWorkerId));
        }
        this.workerId = workerId;
    }

    public static Snowflake getInstance(long workerId) {
        return new Snowflake(workerId);
    }

    @Override
    public synchronized long nextId() {
        long timestamp = this.timeGen();
        if (this.lastTimestamp == timestamp) {
            this.sequence = this.sequence + 1 & this.sequenceMask;
            if (this.sequence == 0) {
                timestamp = this.tilNextMillis(this.lastTimestamp);
            }
        } else {
            this.sequence = 0;
        }
        if (timestamp < this.lastTimestamp) {
            throw new RuntimeException(String.format(
                            "Clock moved backwards. Refusing to generate id for %d milliseconds.",
                            this.lastTimestamp - timestamp));
        }
        this.lastTimestamp = timestamp;
        return (timestamp - twepoch)
                << timestampLeftShift
                | this.workerId
                << workerIdShift
                | this.sequence;
    }

    @Override
    public long[] nextIds(int size) {
        if ((size <= 0) || (size > MAX_SIZE)) {
            throw new IllegalArgumentException(String.format(
                    "size is illegal, size:[1-%d]", MAX_SIZE));
        }
        long[] ids = new long[size];
        for (int i = 0; i < size; i++) {
            ids[i] = nextId();
        }
        return ids;
    }

    public static long getMaxWorkerId() {
        return maxWorkerId;
    }

    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
