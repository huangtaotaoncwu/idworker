package priv.htt.dst;

import org.junit.Test;
import priv.htt.dst.exception.LifecycleException;
import priv.htt.dst.idworker.DstIdWorker;
import priv.htt.dst.idworker.Snowflake;

public class IdWorkerTest {

    @Test
    public void snowflakeTest() {
        long workerId = 1023;
        Snowflake snowflake = Snowflake.getInstance(workerId);
        for (int i = 0; i < 1000; i++) {
            System.out.println(snowflake.nextId());
        }
    }

    @Test
    public void idWorkerTest() throws LifecycleException, InterruptedException {
        DstIdWorker idWorker = new DstIdWorker(
                "192.168.99.110:2181,192.168.99.110:2182,192.168.99.110:2183",
                "dstid");
        idWorker.start();
        try {
            int count = 10;
            while (count-- > 0) {
                Thread.sleep(1000);
                try {
                    System.out.println(idWorker.nextId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            idWorker.stop();
        }
    }

}
