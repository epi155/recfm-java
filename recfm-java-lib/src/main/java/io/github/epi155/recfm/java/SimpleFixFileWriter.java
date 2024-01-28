package io.github.epi155.recfm.java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Simple class for writing a fixed-width file.
 * <p>
 * Let {@code Foo} be the class with which to interpret the file record.
 * The file can be written using code like:
 * <pre>
 * try (SimpleFixFileWriter&lt;Foo&gt; wr = new SimpleFixFileWriter&lt;&gt;(file)) {
 *     Foo foo = ...
 *     wr.write(foo);
 * }
 * </pre>
 *
 * @param <T> fixed-width class
 */
public class SimpleFixFileWriter<T extends FixBasic> implements AutoCloseable {
    private final BufferedWriter bw;

    public SimpleFixFileWriter(File file, Charset cs) throws IOException {
        this.bw = Files.newBufferedWriter(file.toPath(), cs);
    }
    public SimpleFixFileWriter(File file) throws IOException {
        this(file, StandardCharsets.UTF_8);
    }

    public void write(T t) throws IOException {
        bw.write(t.encode());
        bw.newLine();
    }

    @Override
    public void close() throws IOException {
        bw.close();
    }
}
