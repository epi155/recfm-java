package io.github.epi155.recfm.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;

public class FixFileReader<T extends FixBasic> implements AutoCloseable, Iterable<T> {
    private final BufferedReader br;
    private final Constructor<T> ctor;

    public FixFileReader(File file, Class<T> claz, Charset cs) throws IOException, NoSuchMethodException {
        this.br = Files.newBufferedReader(file.toPath(), cs);
        this.ctor = claz.getConstructor(String.class);
    }
    public FixFileReader(File file, Class<T> claz) throws IOException, NoSuchMethodException {
        this(file, claz, StandardCharsets.UTF_8);
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
                        readyItem = ctor.newInstance(line);
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
