package com.example.testj;

import com.example.sysj.test.FooDate;
import com.example.sysj.test.FooTest;
import io.github.epi155.recfm.java.FixError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestFields {

    private FooTest foo;

    @BeforeEach
    public void init() {
        this.foo = new FooTest();
    }
    @Test
    void testAbc() {
        FixError.failFirst();

        foo.setAlpha01("A");
        Assertions.assertEquals("A         ", foo.getAlpha01(), "test align/pad");
        foo.setAlpha01("precipitevolissimevolmente");
        Assertions.assertEquals("precipitev", foo.getAlpha01(), "test align/truncate");

        Assertions.assertThrows(FixError.NotAsciiException.class, () -> foo.setAlpha01("Niña"), "test not ascii (default)");
        Assertions.assertThrows(FixError.NotAsciiException.class, () -> foo.setAlpha02("Niña"), "test not ascii");

        Assertions.assertThrows(FixError.NotLatinException.class, () -> foo.setAlpha03("10 €"), "test not latin1");
        Assertions.assertDoesNotThrow(() -> foo.setAlpha03("Niña"), "test latin1");

        Assertions.assertThrows(FixError.NotValidException.class, () -> foo.setAlpha04("Los\u2fe0Ageles"), "test not valid");
        Assertions.assertDoesNotThrow(() -> foo.setAlpha04("10 €"), "test valid");

        Assertions.assertDoesNotThrow(() -> foo.setAlpha05("Los\u2fe0Ageles"), "test no check");
    }

    @Test
    void testNum() {
        foo.setDigit01(12);
        Assertions.assertEquals("00012", foo.getDigit01(), "test align/pad");
        foo.setDigit01("1415926535897932384626433832");
        Assertions.assertEquals("33832", foo.getDigit01(), "test align/truncate");
        Assertions.assertThrows(FixError.NotDigitException.class, () -> foo.setDigit01("one"), "test digit");
    }

    @Test
    void testCus() {
        foo.setCustom01("12");
        Assertions.assertEquals("00012", foo.getCustom01(), "test align/pad");
        foo.setCustom01("1415926535897932384626433832");
        Assertions.assertEquals("33832", foo.getCustom01(), "test align/truncate");
        Assertions.assertThrows(FixError.NotDigitException.class, () -> foo.setCustom01("three"), "test no digit");
        Assertions.assertThrows(FixError.NotDigitException.class, () -> {
            foo.setCustom01(" ");   // -> "0000 "
        }, "test no digit");
        Assertions.assertThrows(FixError.NotBlankException.class, () -> foo.setCustom01(" 1234"), "test no blank");
        Assertions.assertDoesNotThrow(() -> foo.setCustom01("     "), "test blank");

        foo.setCustom02("12");
        Assertions.assertEquals("00012", foo.getCustom02(), "test align/pad");
        foo.setCustom02("1415926535897932384626433832");
        Assertions.assertEquals("33832", foo.getCustom02(), "test align/truncate");
        Assertions.assertThrows(FixError.NotDigitException.class, () -> foo.setCustom02("three"), "test no digit");
        Assertions.assertThrows(FixError.NotDigitException.class, () -> {
            foo.setCustom02(" ");   // -> "0000 "
        }, "test no digit");
        Assertions.assertThrows(FixError.NotDigitException.class, () -> foo.setCustom02(" 1234"), "test blank head");
        Assertions.assertThrows(FixError.NotDigitException.class, () -> foo.setCustom02("     "), "test blank full");

        foo.setCustom03("12");
        Assertions.assertEquals("12   ", foo.getCustom03(), "test align/pad");
        foo.setCustom03("1415926535897932384626433832");
        Assertions.assertEquals("14159", foo.getCustom03(), "test align/truncate");
        Assertions.assertDoesNotThrow(() -> foo.setCustom03("three"), "test plain ascii");
        Assertions.assertThrows(FixError.NotAsciiException.class, () -> foo.setCustom03("Niña"), "test not ascii");

        foo.setCustom07("12");
        Assertions.assertEquals("12   ", foo.getCustom07(), "test align/pad");
        foo.setCustom07("1415926535897932384626433832");
        Assertions.assertEquals("14159", foo.getCustom07(), "test align/truncate");
        Assertions.assertThrows(FixError.NotMatchesException.class, () -> foo.setCustom07("three"), "test not regex");

    }

    @Test
    void testDom() {
        Assertions.assertEquals("EUR", foo.getDomain01(), "test default");

        foo.setDomain01("USD");
        Assertions.assertEquals("USD", foo.getDomain01(), "test match");

        Assertions.assertThrows(FixError.NotDomainException.class, () -> foo.setDomain01("EURO"), "test not domain");
        Assertions.assertThrows(FixError.NotDomainException.class, () -> foo.setDomain01("AUD"), "test not domain");
        Assertions.assertDoesNotThrow(() -> foo.setHackDom1("AUD"), "test value falsified (redefines)");
        Assertions.assertThrows(FixError.NotDomainException.class, () -> foo.getDomain01(), "test get failure");

        foo.validateFails(it -> {
            System.out.printf("Error on field %s at offset %d, length %d, code %s%n",
                it.name(), it.offset(), it.length(), it.code().name());
            System.out.printf("Value: /%s/%n", it.value());
            System.out.println(it.message());
        });
    }
    @Test
    void testGrp() {
        foo.group01().setAlpha01("HELLO");
        Assertions.assertEquals("HELLO     ", foo.group01().getAlpha01(), "test align/pad");
        foo.group01().setAlpha01("HELLO WORLD");
        Assertions.assertEquals("HELLO WORL", foo.group01().getAlpha01(), "test align/truncate");
        Assertions.assertThrows(FixError.NotAsciiException.class, () -> foo.group01().setAlpha01("привет"), "test ascii");

        foo.group01().setDigit01("12");
        Assertions.assertEquals("00012", foo.group01().getDigit01(), "test align/pad");
        foo.group01().setDigit01("1415926535897932384626433832");
        Assertions.assertEquals("33832", foo.group01().getDigit01(), "test align/truncate");
        Assertions.assertThrows(FixError.NotDigitException.class, () -> foo.group01().setDigit01("one"), "test digit");

        foo.group01().setCustom01("12");
        Assertions.assertEquals("12   ", foo.group01().getCustom01(), "test align/pad");
        foo.group01().setCustom01("1415926535897932384626433832");
        Assertions.assertEquals("14159", foo.group01().getCustom01(), "test align/truncate");
        Assertions.assertDoesNotThrow(() -> foo.group01().setCustom01("three"), "test plain ascii");
        Assertions.assertThrows(FixError.NotAsciiException.class, () -> foo.group01().setCustom01("Niña"), "test not ascii");
    }
    @Test
    void testOcc() {
        foo.errors().setCount(2);
        foo.errors().item(1).setCode("NUL-PTR");
        foo.errors().item(1).setMessage("Null Pointer");
        foo.errors().item(2).setCode("STK-OVF");
        foo.errors().item(2).setMessage("Stack Overflow");

        FooTest.Errors err = foo.errors();
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> err.item(0), "test under-bound");
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> err.item(6), "test over-bound");
    }
    @Test
    void testErr1() {
        FooDate fDate = FooDate.decode("£023-12-\u00001");
        if (!fDate.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
    }

}
