package com.example.testj;

import com.example.sysj.test.Alamos;
import io.github.epi155.recfm.java.FixFileReader;
import org.junit.jupiter.api.Test;

import java.io.File;
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
        File file = new File("/tmp/demo.txt");
        try (FixFileReader<Alamos> rr = new FixFileReader<>(file, Alamos.class)) {
            rr.forEach(alamos -> System.out.println(alamos.getBlu()));
        } catch (Exception e) {
            LOG.severe(dump(e));
        }
    }

}
