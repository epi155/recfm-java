package com.example.testj;

import com.example.sysj.test.*;
import io.github.epi155.recfm.java.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.CharBuffer;
import java.util.logging.Logger;

class TestFields {
    private static final Logger LOG = Logger.getLogger( TestFields.class.getName() );
    private static String dump(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    private FooTest foo;

    @BeforeEach
    public void init() {
        this.foo = new FooTest();
    }
    @Test
    void testAbc() {
        //FixError.failFirst();

        foo.setAlpha01("A");
        Assertions.assertEquals("A         ", foo.getAlpha01(), "test align/pad");
        foo.setAlpha01("precipitevolissimevolmente");
        Assertions.assertEquals("precipitev", foo.getAlpha01(), "test align/truncate");

        Assertions.assertThrows(NotAsciiException.class, () -> foo.setAlpha01("Niña"), "test not ascii (default)");
        Assertions.assertThrows(NotAsciiException.class, () -> foo.setAlpha02("Niña"), "test not ascii");

        Assertions.assertThrows(NotLatinException.class, () -> foo.setAlpha03("10 €"), "test not latin1");
        Assertions.assertDoesNotThrow(() -> foo.setAlpha03("Niña"), "test latin1");

        Assertions.assertThrows(NotValidException.class, () -> foo.setAlpha04("Los\u2fe0Ageles"), "test not valid");
        Assertions.assertDoesNotThrow(() -> foo.setAlpha04("10 €"), "test valid");

        Assertions.assertDoesNotThrow(() -> foo.setAlpha05("Los\u2fe0Ageles"), "test no check");
    }

    @Test
    void testNum() {
        foo.setDigit01(12);
        Assertions.assertEquals("00012", foo.getDigit01(), "test align/pad");
        foo.setDigit01("1415926535897932384626433832");
        Assertions.assertEquals("33832", foo.getDigit01(), "test align/truncate");
        Assertions.assertThrows(NotDigitException.class, () -> foo.setDigit01("one"), "test digit");
    }

    @Test
    void testCus() {
        foo.setCustom01("12");
        Assertions.assertEquals("00012", foo.getCustom01(), "test align/pad");
        foo.setCustom01("1415926535897932384626433832");
        Assertions.assertEquals("33832", foo.getCustom01(), "test align/truncate");
        Assertions.assertThrows(NotDigitBlankException.class, () -> foo.setCustom01("three"), "test no digit");
        Assertions.assertThrows(NotDigitException.class, () -> {
            foo.setCustom01(" ");   // -> "0000 "
        }, "test no digit");
        Assertions.assertThrows(NotBlankException.class, () -> foo.setCustom01(" 1234"), "test no blank");
        Assertions.assertDoesNotThrow(() -> foo.setCustom01("     "), "test blank");

        foo.setCustom02("12");
        Assertions.assertEquals("00012", foo.getCustom02(), "test align/pad");
        foo.setCustom02("1415926535897932384626433832");
        Assertions.assertEquals("33832", foo.getCustom02(), "test align/truncate");
        Assertions.assertThrows(NotDigitException.class, () -> foo.setCustom02("three"), "test no digit");
        Assertions.assertThrows(NotDigitException.class, () -> {
            foo.setCustom02(" ");   // -> "0000 "
        }, "test no digit");
        Assertions.assertThrows(NotDigitException.class, () -> foo.setCustom02(" 1234"), "test blank head");
        Assertions.assertThrows(NotDigitException.class, () -> foo.setCustom02("     "), "test blank full");

        foo.setCustom03("12");
        Assertions.assertEquals("12   ", foo.getCustom03(), "test align/pad");
        foo.setCustom03("1415926535897932384626433832");
        Assertions.assertEquals("14159", foo.getCustom03(), "test align/truncate");
        Assertions.assertDoesNotThrow(() -> foo.setCustom03("three"), "test plain ascii");
        Assertions.assertThrows(NotAsciiException.class, () -> foo.setCustom03("Niña"), "test not ascii");

        foo.setCustom07("12");
        Assertions.assertEquals("12   ", foo.getCustom07(), "test align/pad");
        foo.setCustom07("1415926535897932384626433832");
        Assertions.assertEquals("14159", foo.getCustom07(), "test align/truncate");
        Assertions.assertThrows(NotMatchesException.class, () -> foo.setCustom07("three"), "test not regex");

    }

    @Test
    void testDom() {
        Assertions.assertEquals("EUR", foo.getDomain01(), "test default");

        foo.setDomain01("USD");
        Assertions.assertEquals("USD", foo.getDomain01(), "test match");

        Assertions.assertThrows(NotDomainException.class, () -> foo.setDomain01("EURO"), "test not domain");
        Assertions.assertThrows(NotDomainException.class, () -> foo.setDomain01("AUD"), "test not domain");
        Assertions.assertDoesNotThrow(() -> foo.setHackDom1("AUD"), "test value falsified (redefines)");
        Assertions.assertThrows(NotDomainException.class, () -> foo.getDomain01(), "test get failure");

            foo.validateFails(it -> {
                System.out.printf("Error on field %s at offset %d, length %d, code %s%n",
                        it.name(), it.offset(), it.length(), it.code().name());
                System.out.printf("Value: /%s/%n", it.value());
                System.out.println(it.message());
            });

        foo.setDomain01(null);
        System.out.println(foo.getDomain01());
    }
    @Test
    void testGrp() {
        foo.group01().setAlpha01("HELLO");
        Assertions.assertEquals("HELLO     ", foo.group01().getAlpha01(), "test align/pad");
        foo.group01().setAlpha01("HELLO WORLD");
        Assertions.assertEquals("HELLO WORL", foo.group01().getAlpha01(), "test align/truncate");
        Assertions.assertThrows(NotAsciiException.class, () -> foo.group01().setAlpha01("привет"), "test ascii");

        foo.group01().setDigit01("12");
        Assertions.assertEquals("00012", foo.group01().getDigit01(), "test align/pad");
        foo.group01().setDigit01("1415926535897932384626433832");
        Assertions.assertEquals("33832", foo.group01().getDigit01(), "test align/truncate");
        Assertions.assertThrows(NotDigitException.class, () -> foo.group01().setDigit01("one"), "test digit");

        foo.group01().setCustom01("12");
        Assertions.assertEquals("12   ", foo.group01().getCustom01(), "test align/pad");
        foo.group01().setCustom01("1415926535897932384626433832");
        Assertions.assertEquals("14159", foo.group01().getCustom01(), "test align/truncate");
        Assertions.assertDoesNotThrow(() -> foo.group01().setCustom01("three"), "test plain ascii");
        Assertions.assertThrows(NotAsciiException.class, () -> foo.group01().setCustom01("Niña"), "test not ascii");
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
        if (!fDate.validateFails(it -> {
                System.out.printf("Error field %s@%d+%d: %s%n",
                    it.name(), it.offset(), it.length(), it.message());
                it.column();
                it.wrong();
            })) {
            System.out.println("Valid Date");
        }
    }

    @Test
    void testAlpha() {
        FooAlpha alpha = new FooAlpha();
        Assertions.assertThrows(FieldUnderFlowException.class, () -> alpha.setStrict(null), "test Abc underflow");
        Assertions.assertThrows(FieldUnderFlowException.class, () -> alpha.setStrict("123"), "test Abc underflow");
        Assertions.assertThrows(FieldOverFlowException.class, () -> alpha.setStrict("12345"), "test Abc overflow");
        Assertions.assertDoesNotThrow(() -> alpha.setWeak(null), "test Abc underflow/init");
        Assertions.assertDoesNotThrow(() -> alpha.setWeak("123"), "test Abc underflow/pad");
        Assertions.assertDoesNotThrow(() -> alpha.setWeak("12345"), "test Abc overflow/trunc");

        Assertions.assertThrows(RecordUnderflowException.class, () -> FooAlpha.decode("123"), "test underflow");
        Assertions.assertThrows(RecordOverflowException.class, () -> FooAlpha.decode("123456789012"), "test overflow");

        Assertions.assertThrows(NotAsciiException.class, () -> alpha.setStrict("\u0000"), "test ASCII");
        Assertions.assertDoesNotThrow(() -> alpha.setWeak(null), "test Latin1");
        Assertions.assertThrows(NotLatinException.class, () -> alpha.setWeak("\u0000"), "test Latin1");
        Assertions.assertDoesNotThrow(() -> alpha.setUtf8(null), "test UTF-8");
        Assertions.assertThrows(NotValidException.class, () -> alpha.setUtf8("\u0000"), "test UTF-8");
        Assertions.assertDoesNotThrow(() -> alpha.setAll("\u0000"), "test no check");
        Assertions.assertDoesNotThrow(() -> alpha.setAll(null), "test no check");

        alpha.getWeak();
        alpha.getUtf8();
        String s = alpha.encode();

        if (!alpha.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                        it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }

        FooAlpha a = FooAlpha.decode(CharBuffer.allocate(10).toString());
        Assertions.assertThrows(NotAsciiException.class, a::getStrict, "test ASCII");
        Assertions.assertThrows(NotLatinException.class, a::getWeak, "test Latin1");
        Assertions.assertThrows(NotValidException.class, a::getUtf8, "test UTF-8");
        Assertions.assertDoesNotThrow(a::getAll, "test no check");

        if (!a.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                        it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        alpha.setAll("\u007f€");
        System.out.println(alpha);
    }
    @Test
    void testDigit() {
        //FixError.failAll();
        FooDigit digit = new FooDigit();

        Assertions.assertThrows(FieldUnderFlowException.class, () -> digit.setStrict(null), "test Num underflow");
        Assertions.assertThrows(FieldUnderFlowException.class, () -> digit.setStrict("123"), "test Num underflow");
        Assertions.assertThrows(FieldOverFlowException.class, () -> digit.setStrict("12345"), "test Num overflow");
        Assertions.assertDoesNotThrow(() -> digit.setWeak(null), "test Num underflow/init");
        Assertions.assertDoesNotThrow(() -> digit.setWeak("123"), "test Num underflow/pad");
        Assertions.assertDoesNotThrow(() -> digit.setWeak("12345"), "test Num overflow/trunc");

        Assertions.assertThrows(NotMatchesException.class, () -> digit.setRex("Hi"), "test Num invalid");

        FooAlpha alpha = FooAlpha.of(digit);    // cast
        FooDigit numer = digit.copy();      // clone / deep-copy

        if (!digit.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                        it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }

        FooDigit n = FooDigit.decode(CharBuffer.allocate(10).toString());
        if (!n.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                        it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        Assertions.assertThrows(NotDigitException.class, n::getStrict, "test Num get");
        Assertions.assertThrows(NotMatchesException.class, n::getRex, "test Num get");

        n.setRex("11");
        System.out.println(n.getRex());
        if (!n.validateFails(it ->
            System.out.printf("Error field %s@%d+%d: %s%n",
                it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        Assertions.assertThrows(NotMatchesException.class, () -> n.setRex(null), "test Num set");

        Assertions.assertDoesNotThrow(() -> FooDigit.decode("123"), "test underflow");
        Assertions.assertDoesNotThrow(() -> FooDigit.decode("123456789012"), "test overflow");
    }
    @Test
    void testCustom() {
        FooCustom cust = new FooCustom();
        Assertions.assertThrows(FieldUnderFlowException.class, () -> cust.setFix(null), "test Cus underflow");
        Assertions.assertThrows(FieldUnderFlowException.class, () -> cust.setFix("a"), "test Cus underflow");
        Assertions.assertThrows(FieldOverFlowException.class, () -> cust.setFix("12345"), "testCus overflow");
        Assertions.assertDoesNotThrow(() -> cust.setFix("ab"), "test Cus fit");
        cust.setLft("a");  System.out.println(cust.getLft());
        cust.setLft("ab");  System.out.println(cust.getLft());
        cust.setLft("abcdefg");  System.out.println(cust.getLft());
        cust.setLft(null);  System.out.println(cust.getLft());

        cust.setRgt("a");  System.out.println(cust.getRgt());
        cust.setRgt("abc");  System.out.println(cust.getRgt());
        cust.setRgt("abcdefg");  System.out.println(cust.getRgt());
        cust.setRgt(null);  System.out.println(cust.getRgt());

        Assertions.assertThrows(NotDigitException.class, () -> cust.setDig("a"), "testCus invalid");

        cust.setDig("1");
        cust.setDig("  ");
        cust.setDig("     ");
        cust.setDig("12345");
        cust.setDig(null);
        System.out.println(cust);
        cust.charAt(1);
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> cust.charAt(0), "test OOB");
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> cust.charAt(11), "test OOB");

        if (!cust.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                        it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }

        FooCustom cu1 = FooCustom.decode(CharBuffer.allocate(10).toString().replace('\u0000', ' '));
        if (!cu1.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                        it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        cu1.getDig();

        FooCustom cu2 = FooCustom.decode(CharBuffer.allocate(10).toString().replace('\u0000', '*'));
        if (!cu2.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                        it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        Assertions.assertThrows(NotDigitBlankException.class, cu2::getDig, "testCus invalid");

        FooCustom cu3 = FooCustom.decode("12345678x0");
        if (!cu3.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                        it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        Assertions.assertThrows(NotDigitException.class, cu3::getDig, "testCus invalid");

        FooCustom cu4 = FooCustom.decode("1234567 x0");
        if (!cu4.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                        it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        Assertions.assertThrows(NotBlankException.class, cu4::getDig, "testCus invalid");

        try {
            cu4.getDig();
        } catch (NotBlankException e) {
            LOG.severe(dump(e));
        }
    }
    @Test
    void testDomain() {
        FooDom dom = new FooDom();
        dom.setCur(null);
        String domNull = dom.getCur();
        Assertions.assertEquals("EUR", domNull);
        Assertions.assertThrows(NotDomainException.class, () -> dom.setCur("AAA"), "test Dom invalid");
        dom.setCur("USD");
        if (!dom.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                        it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }

        FooDom d1 = FooDom.decode("AAA");
        if (!d1.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                        it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        Assertions.assertThrows(NotDomainException.class, d1::getCur, "test Dom invalid");

    }
}
