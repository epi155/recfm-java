package com.example.testj;

import it.insur.recfm.TestZ;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

class TestRead {
    private static final Logger LOG = Logger.getLogger( TestRead.class.getName() );

    @Test
    void testDump() {
        TestZ tz = new TestZ();
        System.out.println(tz.toString());
    }
}
