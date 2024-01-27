package io.github.epi155.recfm.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;

public class SimpleFixFileReader<T extends FixBasic> implements AutoCloseable, Iterable<T> {
    private final BufferedReader br;
    private final FixDecoder<T> decoder;

    public SimpleFixFileReader(File file, FixDecoder<T> decoder, Charset cs) throws IOException {
        this.br = Files.newBufferedReader(file.toPath(), cs);
        this.decoder = decoder;
    }
    public SimpleFixFileReader(File file, FixDecoder<T> decoder) throws IOException {
        this(file, decoder, StandardCharsets.UTF_8);
    }
    @Override
    public void close() throws IOException {
        br.close();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private T readyItem;
            @Override
            public boolean hasNext() {
                try {
                    String line = br.readLine();
                    if (line == null) {
                        readyItem = null;
                        return false;
                    } else {
                        readyItem = decoder.decode(line);
                        return  true;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public T next() {
                T swap = readyItem;
                readyItem = null;
                return swap;
            }
        };
    }
}
