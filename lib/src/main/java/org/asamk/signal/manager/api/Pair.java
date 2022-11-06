package org.asamk.signal.manager.api;

public class Pair<T, U> {

    public T first;
    public U second;

    public T first() {
        return first;
    }

    public U second() {
        return second;
    }

    public Pair(T first, U second) {
        super();
        this.first = first;
        this.second = second;
    }
}
