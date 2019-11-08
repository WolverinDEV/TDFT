package dev.wolveringer.tdft;

import java.util.Comparator;

public interface ResultComparator<T> extends Comparator<T> {
    @Override
    default int compare(T o1, T o2) {
        return this.matches(o1, o2) ? 0 : -1;
    }

    boolean matches(T o1, T o2);
}
