package com.example.testj;

import com.example.sysj.test.BarAlpha;
import com.example.sysj.test.BarCustom;
import com.example.sysj.test.BarDigit;
import com.example.sysj.test.BarDom;
import io.github.epi155.recfm.java.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.CharBuffer;

public class TestBar {
    @Test
    void testAlpha() {
        BarAlpha alpha = new BarAlpha();
        Assertions.assertThrows(FieldUnderFlowException.class, () -> alpha.setStrict(null), "test Abc underflow");
        Assertions.assertThrows(FieldUnderFlowException.class, () -> alpha.setStrict("123"), "test Abc underflow");
        Assertions.assertThrows(FieldOverFlowException.class, () -> alpha.setStrict("12345"), "test Abc overflow");
        Assertions.assertDoesNotThrow(() -> alpha.setWeak(null), "test Abc underflow/init");
        Assertions.assertDoesNotThrow(() -> alpha.setWeak("123"), "test Abc underflow/pad");
        Assertions.assertDoesNotThrow(() -> alpha.setWeak("12345"), "test Abc overflow/trunc");

        Assertions.assertThrows(RecordUnderflowException.class, () -> BarAlpha.decode("123"), "test underflow");
        Assertions.assertThrows(RecordOverflowException.class, () -> BarAlpha.decode("123456789012"), "test overflow");

        Assertions.assertThrows(FieldUnderFlowException.class, () -> alpha.setStrict("\u0000"), "test ASCII");
        Assertions.assertDoesNotThrow(() -> alpha.setWeak(null), "test Latin1");
        Assertions.assertDoesNotThrow(() -> alpha.setWeak("\u0000"), "test Latin1");
        Assertions.assertDoesNotThrow(() -> alpha.setUtf8(null), "test UTF-8");
        Assertions.assertDoesNotThrow(() -> alpha.setUtf8("\u0000"), "test UTF-8");
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

        BarAlpha a = BarAlpha.decode(CharBuffer.allocate(10).toString());
        Assertions.assertDoesNotThrow(() -> a.getStrict(), "test ASCII");
        Assertions.assertDoesNotThrow(() -> a.getWeak(), "test Latin1");
        Assertions.assertDoesNotThrow(() -> a.getUtf8(), "test UTF-8");
        Assertions.assertDoesNotThrow(() -> a.getAll(), "test no check");

        if (!a.validateFails(it ->
            System.out.printf("Error field %s@%d+%d: %s%n",
                it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        alpha.setWeak("abc  ");
        Assertions.assertEquals("abc", alpha.getWeak());
        alpha.setWeak(" ");
        Assertions.assertEquals("", alpha.getWeak());

        alpha.setUtf8("  ");
        Assertions.assertEquals(" ", alpha.getUtf8());
    }
    @Test
    void testDigit() {
        FixError.failAll();
        BarDigit digit = new BarDigit();

        Assertions.assertThrows(FieldUnderFlowException.class, () -> digit.setStrict(null), "test Num underflow");
        Assertions.assertThrows(FieldUnderFlowException.class, () -> digit.setStrict("123"), "test Num underflow");
        Assertions.assertThrows(FieldOverFlowException.class, () -> digit.setStrict("12345"), "test Num overflow");
        Assertions.assertDoesNotThrow(() -> digit.setWeak(null), "test Num underflow/init");
        Assertions.assertDoesNotThrow(() -> digit.setWeak("123"), "test Num underflow/pad");
        Assertions.assertDoesNotThrow(() -> digit.setWeak("12345"), "test Num overflow/trunc");

        Assertions.assertDoesNotThrow(() -> digit.setRex("Hi"), "test Num invalid");
        ;

        BarAlpha alpha = BarAlpha.of(digit);    // cast
        BarDigit numer = digit.copy();      // clone / deep-copy

        if (!digit.validateFails(it ->
            System.out.printf("Error field %s@%d+%d: %s%n",
                it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }

        BarDigit n = BarDigit.decode(CharBuffer.allocate(10).toString());
        if (!n.validateFails(it ->
            System.out.printf("Error field %s@%d+%d: %s%n",
                it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        Assertions.assertThrows(NotDigitException.class, () -> n.getStrict(), "test Num get");
        Assertions.assertDoesNotThrow(() -> n.getRex(), "test Num get");

        n.setRex("11");
        System.out.println(n.getRex());
        if (!n.validateFails(it ->
            System.out.printf("Error field %s@%d+%d: %s%n",
                it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        Assertions.assertDoesNotThrow(() -> n.setRex(null), "test Num set");

        Assertions.assertDoesNotThrow(() -> BarDigit.decode("123"), "test underflow");
        Assertions.assertDoesNotThrow(() -> BarDigit.decode("123456789012"), "test overflow");

        digit.setWeak("0000");
        Assertions.assertEquals("0", digit.getWeak());
        digit.setWeak("0100");
        Assertions.assertEquals("100", digit.getWeak());
    }
    @Test
    void testCustom() {
        BarCustom cust = new BarCustom();
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

        Assertions.assertDoesNotThrow(() -> cust.setDig("a"), "testCus invalid");

        cust.setDig("1");
        cust.setDig("  ");
        cust.setDig("     ");
        cust.setDig("12345");
        cust.setDig(null);
        System.out.println(cust.toString());
        cust.charAt(1);
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> cust.charAt(0), "test OOB");
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> cust.charAt(11), "test OOB");

        if (!cust.validateFails(it ->
            System.out.printf("Error field %s@%d+%d: %s%n",
                it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }

        BarCustom cu1 = BarCustom.decode(CharBuffer.allocate(10).toString().replace('\u0000', ' '));
        if (!cu1.validateFails(it ->
            System.out.printf("Error field %s@%d+%d: %s%n",
                it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        cu1.getDig();

        BarCustom cu2 = BarCustom.decode(CharBuffer.allocate(10).toString().replace('\u0000', '*'));
        if (!cu2.validateFails(it ->
            System.out.printf("Error field %s@%d+%d: %s%n",
                it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        Assertions.assertDoesNotThrow(() -> cu2.getDig(), "testCus invalid");

        BarCustom cu3 = BarCustom.decode("12345678x0");
        if (!cu3.validateFails(it ->
            System.out.printf("Error field %s@%d+%d: %s%n",
                it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        Assertions.assertDoesNotThrow(() -> cu3.getDig(), "testCus invalid");

        BarCustom cu4 = BarCustom.decode("1234567 x0");
        if (!cu4.validateFails(it ->
            System.out.printf("Error field %s@%d+%d: %s%n",
                it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        Assertions.assertDoesNotThrow(() -> cu4.getDig(), "testCus invalid");

        try {
            cu4.getDig();
        } catch (NotBlankException e) {
            e.printStackTrace();
        }

        cust.setRgt("00");
        Assertions.assertEquals("", cust.getRgt());
        cust.setRgt("010");
        Assertions.assertEquals("10", cust.getRgt());

        cust.setLft("**");
        Assertions.assertEquals("", cust.getLft());
        cust.setLft("*1*");
        Assertions.assertEquals("*1", cust.getLft());
    }
    @Test
    void testDomain() {
        BarDom dom = new BarDom();
        dom.setCur(null);
        String domNull = dom.getCur();
        Assertions.assertEquals("EUR", domNull);
        Assertions.assertThrows(NotDomainException.class, () -> dom.setCur("AAA"), "test Dom invalid");
        dom.setCur("USD");

        BarDom d1 = BarDom.decode("AAA");
        if (!d1.validateFails(it ->
                System.out.printf("Error field %s@%d+%d: %s%n",
                        it.name(), it.offset(), it.length(), it.message()))) {
            System.out.println("Valid Date");
        }
        Assertions.assertThrows(NotDomainException.class, () -> d1.getCur(), "test Dom invalid");

    }
}
