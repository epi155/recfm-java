package io.github.epi155.recfm.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;

/**
 * Simple class for reading a fixed-width file.
 * <p>
 * Let {@code Foo} be the class with which to interpret the file record.
 * The file can be read using code like:
 * <pre>
 * try (SimpleFixFileReader&lt;Foo&gt; rd = new SimpleFixFileReader&lt;&gt;(file, Foo::decode)) {
 *     rd.forEach(foo -&gt; ...);
 * }
 * </pre>
 * to use the class with java 7 code a few more lines of code are needed:
 * <pre>
 * try (SimpleFixFileReader&lt;Foo&gt; rd = new SimpleFixFileReader&lt;&gt;(file, new FixDecoder&lt;Foo&gt;() {
 *     &#64;Override
 *     public Foo decode(String line) {
 *         return Foo.decode(line);
 *     }
 * })) {
 *     Iterator&lt;Foo&gt; iterator = rd.iterator();
 *     while (iterator.hasNext()) {
 *         Foo foo = iterator.next();
 *         ...
 *     }
 * }
 * </pre>
 *
 * @param <T> fixed-width class
 */
public class SimpleFixFileReader<T extends FixBasic> implements AutoCloseable, Iterable<T> {
    private final BufferedReader br;
    private final FixDecoder<T> decoder;

    /**
     * Class full constructor.
     *
     * @param file  fixed-width file
     * @param decoder   decoder String to Class
     * @param cs    file charset
     * @throws IOException IO error
     */
    public SimpleFixFileReader(File file, FixDecoder<T> decoder, Charset cs) throws IOException {
        this.br = Files.newBufferedReader(file.toPath(), cs);
        this.decoder = decoder;
    }

    /**
     * Class constructor with default charset (UTF-8).
     *
     * @param file  fixed-width file
     * @param decoder   decoder String to Class
     * @throws IOException IO error
     */
    public SimpleFixFileReader(File file, FixDecoder<T> decoder) throws IOException {
        this(file, decoder, StandardCharsets.UTF_8);
    }

    /**
     * Close file
     *
     * @throws IOException IO error
     */
    @Override
    public void close() throws IOException {
        br.close();
    }

    /**
     * iterator to loop through the file
     *
     * @return class iterator instance
     */
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
