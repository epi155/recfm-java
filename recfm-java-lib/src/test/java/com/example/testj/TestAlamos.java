package com.example.testj;

import com.example.sysj.test.Alamos;
import com.example.sysj.test.Foo311b;
import io.github.epi155.recfm.java.NotDigitBlankException;
import io.github.epi155.recfm.java.WithAction;
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
    }
    @Test
    void testSet1() {
        Alamos a = new Alamos();
        a.setRed(null);
        Assertions.assertEquals("    ", a.getXred());
        a.setCya(null);
        Assertions.assertEquals("    ", a.getXcya());
        Assertions.assertNull(a.getCya());
    }
    @Test
    void testGrp01() {
        Foo311b foo = new Foo311b();
        foo.stopTime().setHours("01");
        foo.withStopTime(stop -> {
            stop.setHours("01");
        });
        foo.withStopTime(new WithAction<Foo311b.StopTime>() {
            @Override
            public void accept(Foo311b.StopTime stop) {
                stop.setHours("01");
            }
        });
        String hours = foo.stopTime().getHours();
        boolean test = foo.validateFails(x -> System.out.println(x.message()));
    }
}
