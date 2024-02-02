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

    /**
     * Class full constructor.
     *
     * @param file fixed-width file
     * @param cs file charset
     * @throws IOException IO error
     */
    public SimpleFixFileWriter(File file, Charset cs) throws IOException {
        this.bw = Files.newBufferedWriter(file.toPath(), cs);
    }

    /**
     * Class constructor with default charset (UTF-8).
     *
     * @param file fixed-width file
     * @throws IOException IO error
     */
    public SimpleFixFileWriter(File file) throws IOException {
        this(file, StandardCharsets.UTF_8);
    }

    /**
     * Class constructor from BufferedWriter.
     *
     * @param bufferedWriter bufferedWriter
     */
    public SimpleFixFileWriter(BufferedWriter bufferedWriter) {
        this.bw = bufferedWriter;
    }

    /**
     * Object writer.
     *
     * @param t object to write
     * @throws IOException IO error
     */
    public void write(T t) throws IOException {
        bw.write(t.encode());
        bw.newLine();
    }

    /**
     * Close file
     *
     * @throws IOException IO error
     */
    @Override
    public void close() throws IOException {
        bw.close();
    }
}
