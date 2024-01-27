package io.github.epi155.recfm.java;

public interface FixDecoder<T> {
    T decode(String line);
}
