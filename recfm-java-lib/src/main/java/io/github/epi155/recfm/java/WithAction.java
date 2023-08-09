package io.github.epi155.recfm.java;

/**
 * interface to handle fields inside a group as a closure
 * @param <T> group type
 */
public interface WithAction<T> {
    /**
     * Performs this operation on the given argument.
     * @param value the input argument
     */
    void accept(T value);
}
