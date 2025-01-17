package shared.models.generics;

import java.io.Serial;
import java.io.Serializable;


public class Pair<T, U> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    private T first;
    private U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(U second) {
        this.second = second;
    }

}