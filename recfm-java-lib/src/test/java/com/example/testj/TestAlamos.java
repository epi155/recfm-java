package com.example.testj;

import com.example.sysj.test.Alamos;
import io.github.epi155.recfm.java.NotDigitBlankException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestAlamos {
    @Test
    void testInit1() {
        String s = "                    ";
        Alamos a = Alamos.decode(s);
        Assertions.assertNull(a.getRed());
        Assertions.assertNull(a.getGre());
        Assertions.assertNull(a.getBlu());
        Assertions.assertNull(a.getYel());
        Assertions.assertNull(a.getCya());
    }
    @Test
    void testInit2() {
        Alamos a = new Alamos();
        Assertions.assertNull(a.getRed());
        Assertions.assertNull(a.getGre());
        Assertions.assertEquals("0000", a.getBlu());
        Assertions.assertEquals("0", a.getYel());
        Assertions.assertNull(a.getCya());
    }
    @Test
    void testInit3() {
        String s = "********************";
        Alamos a = Alamos.decode(s);
        a.validateFails(x -> System.out.println(x.name()+ "@"+x.offset()+"+"+x.length()+": "+x.message()));
        Assertions.assertThrows(NotDigitBlankException.class, a::getRed);
        Assertions.assertThrows(NotDigitBlankException.class, a::getGre);
        Assertions.assertThrows(NotDigitBlankException.class, a::getBlu);
        Assertions.assertThrows(NotDigitBlankException.class, a::getYel);
        Assertions.assertThrows(NotDigitBlankException.class, a::getCya);
        Assertions.assertThrows(NotDigitBlankException.class, a::intCya);
    }
    @Test
    void testSet1() {
        Alamos a = new Alamos();
        a.setRed(null);
        Assertions.assertEquals("    ", a.getXred());
        a.setCya((Integer)null);
        Assertions.assertEquals("    ", a.getXcya());
        Assertions.assertNull(a.getCya());
        Assertions.assertNull(a.intCya());
    }
}
