package pt.ulisboa.tecnico.captor.captorsharedlibrary.evaluation;

public class Timer {

    private long start;
    private long end;

    public long getDifference() {       // in ms
        return (end - start) / 1000000;
    }

    public void run() {
        this.start = System.nanoTime();
    }

    public void stop() {
        this.end = System.nanoTime();
    }

}
