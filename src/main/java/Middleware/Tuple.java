package Middleware;

/**
 * Class to be used when a tuple is needed
 * */
public class Tuple<F, S> {
    private final F first;
    private final S second;

    public Tuple(F x, S y) {
        this.first = x;
        this.second = y;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
}
