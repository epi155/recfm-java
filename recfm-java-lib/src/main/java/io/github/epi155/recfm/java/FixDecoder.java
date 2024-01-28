package io.github.epi155.recfm.java;

/**
 * Interface to indicate how to decode a file with fixed width.
 * Used for pre-java8 compatibility,
 * it is essentially equivalent to {@code Function<String,T>}
 * @param <T> fixed-width class
 */
public interface FixDecoder<T> {
    /**
     * Decoder String to T
     * @param line  String from file
     * @return  instance of the class with the set values
     */
    T decode(String line);
}
