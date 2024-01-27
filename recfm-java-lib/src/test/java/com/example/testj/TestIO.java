package com.example.testj;

import com.example.sysj.test.Alamos;
import io.github.epi155.recfm.java.FixDecoder;
import io.github.epi155.recfm.java.SimpleFixFileReader;
import io.github.epi155.recfm.java.SimpleFixFileWriter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
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


        try (SimpleFixFileReader<Alamos> rr = new SimpleFixFileReader<>(iFile, new FixDecoder<Alamos>() {
            @Override
            public Alamos decode(String line) {
                return Alamos.decode(line);
            }
        })) {
            Iterator<Alamos> iterator = rr.iterator();
            while (iterator.hasNext()) {
                Alamos alamos = iterator.next();
            }
        } catch (IOException e) {
            LOG.severe(dump(e));
        }

        try (SimpleFixFileWriter<Alamos> wr = new SimpleFixFileWriter<>(oFile)) {
            Alamos alamos = new Alamos();
            wr.write(alamos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
