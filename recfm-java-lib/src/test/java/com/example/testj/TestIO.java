package com.example.testj;

import com.example.sysj.test.Alamos;
import io.github.epi155.recfm.java.SimpleFixFileReader;
import io.github.epi155.recfm.java.SimpleFixFileWriter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class TestIO {
    private static final Logger LOG = Logger.getLogger( TestBar.class.getName() );
    private static String dump(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
    @Test
    void testReadWrite() {
        File iFile = new File("/tmp/demo1.txt");
        File oFile = new File("/tmp/demo2.txt");
        try (SimpleFixFileReader<Alamos> rr = new SimpleFixFileReader<>(iFile, Alamos::decode)) {
            rr.forEach(alamos -> System.out.println(alamos.getBlu()));
        } catch (IOException e) {
            LOG.severe(dump(e));
        }

        Alamos alamos = new Alamos();
        try (SimpleFixFileWriter<Alamos> ww = new SimpleFixFileWriter<>(oFile)) {
            ww.write(alamos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
