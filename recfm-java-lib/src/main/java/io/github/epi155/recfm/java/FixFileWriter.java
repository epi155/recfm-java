package io.github.epi155.recfm.java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FixFileWriter<T extends FixBasic> implements AutoCloseable {
    private final BufferedWriter bw;

    public FixFileWriter(File file, Charset cs) throws IOException {
        this.bw = Files.newBufferedWriter(file.toPath(), cs);
    }
    public FixFileWriter(File file) throws IOException {
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
