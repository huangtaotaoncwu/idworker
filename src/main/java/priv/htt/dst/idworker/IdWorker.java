package priv.htt.dst.idworker;

public interface IdWorker {

    long nextId();

    long[] nextIds(int size);

}
