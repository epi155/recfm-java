package io.github.epi155.recfm.java;

import java.nio.CharBuffer;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.epi155.recfm.java.FixError.FAIL_FIRST;

abstract class FixEngine {
    private static final String FIELD_AT = "Field @";
    private static final String EXPECTED = " expected ";
    private static final String CHARS_FOUND = " chars , found ";
    private static final String RECORD_LENGTH = "Record length ";
    protected final char[] rawData;

    protected FixEngine(int length) {
        this.rawData = new char[length];
    }

    protected FixEngine(char[] c, int lrec, boolean overflowError, boolean underflowError) {
        if (c.length == lrec) {
            rawData = c;
        } else if (c.length > lrec) {
            if (overflowError)
                throw new FixError.RecordOverflowException(RECORD_LENGTH + c.length + EXPECTED + lrec);
            rawData = Arrays.copyOfRange(c, 0, lrec);
        } else {
            if (underflowError)
                throw new FixError.RecordUnderflowException(RECORD_LENGTH + c.length + EXPECTED + lrec);
            this.rawData = new char[lrec];
            initialize();
            System.arraycopy(c, 0, rawData, 0, c.length);
        }
    }

    private static boolean isBlank(final CharSequence cs) {
        final int strLen = cs == null ? 0 : cs.length();
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    protected abstract void initialize();

    protected String getAbc(int offset, int count) {
        return new String(rawData, offset, count);
    }

    protected static void testDigit(String value) {
        if (value == null) return;
        char[] raw = value.toCharArray();
        for (int u = 0; u < raw.length; u++) {
            char c = raw[u];
            if (!('0' <= c && c <= '9')) {
                throw new FixError.NotDigitException(c, u + 1);
            }
        }
    }

    protected String spaceNull(String s) {
        return isBlank(s) ? null : s;
    }

    protected static void testAscii(String value) {
        if (value == null) return;
        char[] raw = value.toCharArray();
        for (int u = 0; u < raw.length; u++) {
            char c = raw[u];
            if (!(32 <= c && c < 127)) {
                throw new FixError.NotAsciiException(c, u + 1);
            }
        }
    }

    protected static void testLatin(String value) {
        if (value == null) return;
        char[] raw = value.toCharArray();
        for (int u = 0; u < raw.length; u++) {
            int c = raw[u];
            if (!(32 <= c && c < 127) && !(160 <= c && c <= 255)) {
                throw new FixError.NotLatinException(c, u + 1);
            }
        }
    }

    protected static void testValid(String value) {
        if (value == null) return;
        char[] raw = value.toCharArray();
        for (int u = 0; u < raw.length; u++) {
            char c = raw[u];
            if (Character.isISOControl(c) || !Character.isDefined(c)) {
                throw new FixError.NotValidException(c, u + 1);
            }
        }
    }

    protected static void testArray(String value, String[] domain) {
        if (value == null) return;
        if (Arrays.binarySearch(domain, value) < 0)
            throw new FixError.NotDomainException(value);
    }

    protected static void testRegex(String value, Pattern pattern) {
        if (value == null) return;
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
            throw new FixError.NotMatchesException(value);
    }

    private void fillChar(int offset, int count, char fill) {
        for (int u = 0, v = offset; u < count; u++, v++) {
            rawData[v] = fill;
        }
    }

    protected static String normalize(String s,
                                      OverflowAction overflowAction,
                                      UnderflowAction underflowAction,
                                      char pad, char init,
                                      int offset, int count) {
        if (s == null) {
            if (underflowAction == UnderflowAction.Error)
                throw new FixError.FieldUnderFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + " null");
            return fill(count, init);
        } else if (s.length() == count)
            return s;
        else if (s.length() < count) {
            switch (underflowAction) {
                case PadR:
                    return rpad(s, count, pad);
                case PadL:
                    return lpad(s, count, pad);
                case Error:
                    throw new FixError.FieldUnderFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + s.length());
            }
        } else switch (overflowAction) {
            case TruncR:
                return rtrunc(s, count);
            case TruncL:
                return ltrunc(s, count);
            case Error:
                throw new FixError.FieldOverFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + s.length());
        }
        return null; // dear branch (?)
    }

    private static String fill(int t, char pad) {
        return CharBuffer.allocate( t ).toString().replace( '\0', pad );
    }

    private static String rpad(String s, int t, char pad) {
        int len = s.length();
        if (len > t) return s.substring(0, t);
        if (len == t) return s;
        return s + CharBuffer.allocate( t-len ).toString().replace( '\0', pad );
    }

    protected void fill(int offset, int count, char c) {
        for (int u = offset, v = 0; v < count; u++, v++) {
            rawData[u] = c;
        }
    }

    private void truncLeft(String s, int offset, int count) {
        for (int u = s.length() - 1, v = offset + count - 1; v >= offset; u--, v--) {
            rawData[v] = s.charAt(u);
        }
    }

    private void truncRight(String s, int offset, int count) {
        for (int u = 0, v = offset; u < count; u++, v++) {
            rawData[v] = s.charAt(u);
        }
    }

    protected NumberFormat pic9(int digits) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(digits);
        nf.setGroupingUsed(false);
        return nf;
    }

    public String encode() {
        return new String(rawData);
    }

    /**
     * Valida tutti i campi
     *
     * @param handler gestore errore
     * @return <b>true</b> in caso di errore, <b>false</b> in assenza di errori
     */
    public boolean validateFails(FieldValidateHandler handler) {
        return validateFields(handler);
    }

    /**
     * Valida i campi marcati con <i>audit</i>: <b>true</b>
     *
     * @param handler gestore errore
     * @return <b>true</b> in caso di errore, <b>false</b> in assenza di errori
     */
    public boolean auditFails(FieldValidateHandler handler) {
        return auditFields(handler);
    }

    protected abstract boolean validateFields(FieldValidateHandler handler);

    protected abstract boolean auditFields(FieldValidateHandler handler);

    protected boolean checkDigit(String name, int offset, int count, FieldValidateHandler handler) {
        boolean fault = false;
        for (int u = offset, v = 0; v < count; u++, v++) {
            char c = rawData[u];
            if (!('0' <= c && c <= '9')) {
                handler.error(FixError.Detail
                        .builder()
                        .name(name)
                        .offset(offset)
                        .length(count)
                        .value(getAbc(offset, count))
                        .column(u)
                        .code(ValidateError.NotNumber)
                        .wrong(c)
                        .build());
                if (FAIL_FIRST) return true; else fault = true;
            }
        }
        return fault;
    }

    protected boolean checkDigitBlank(String name, int offset, int count, FieldValidateHandler handler) {
        boolean fault = false;
        char c = rawData[offset];
        if (c == ' ') {
            for (int u = offset + 1, v = 1; v < count; u++, v++) {
                if (rawData[u] != ' ') {
                    handler.error(FixError.Detail
                            .builder()
                            .name(name)
                            .offset(offset)
                            .length(count)
                            .value(getAbc(offset, count))
                            .column(u)
                            .code(ValidateError.NotBlank)  // ??
                            .wrong(c)
                            .build());
                    if (FAIL_FIRST) return true; else fault = true;
                }
            }
        } else if ('0' <= c && c <= '9') {
            for (int u = offset + 1, v = 1; v < count; u++, v++) {
                c = rawData[u];
                if (!('0' <= c && c <= '9')) {
                    handler.error(FixError.Detail
                            .builder()
                            .name(name)
                            .offset(offset)
                            .length(count)
                            .value(getAbc(offset, count))
                            .column(u)
                            .code(ValidateError.NotNumber)
                            .wrong(c)
                            .build());
                    if (FAIL_FIRST) return true; else fault = true;
                }
            }
        } else {
            return true;
        }
        return fault;
    }

    private static String lpad(String s, int t, char pad) {
        int len = s.length();
        if (len > t) return s.substring(len-t);
        if (len == t) return s;
        return CharBuffer.allocate( t-len ).toString().replace( '\0', pad ) + s;
    }

    private void padToLeft(String s, int offset, int count, char c) {
        int u = s.length() - 1;
        int v = offset + count - 1;
        for (; u >= 0; u--, v--) {
            rawData[v] = s.charAt(u);
        }
        for (; v >= offset; v--) {
            rawData[v] = c;
        }
    }

    private static String rtrunc(String s, int t) {
        int len = s.length();
        return (len > t) ? s.substring(0, t) : s;
    }
    protected boolean checkAscii(String name, int offset, int count, FieldValidateHandler handler) {
        boolean fault = false;
        for (int u = offset, v = 0; v < count; u++, v++) {
            char c = rawData[u];
            if (!(32 <= c && c < 127)) {
                handler.error(FixError.Detail
                        .builder()
                        .name(name)
                        .offset(offset)
                        .length(count)
                        .value(getAbc(offset, count))
                        .column(u)
                        .code(ValidateError.NotAscii)
                        .wrong(c)
                        .build());
                if (FAIL_FIRST) return true; else fault = true;
            }
        }
        return fault;
    }

    private void padToRight(String s, int offset, int count, char c) {
        int u = 0;
        int v = offset;
        for (; u < s.length(); u++, v++) {
            rawData[v] = s.charAt(u);
        }
        for (; u < count; u++, v++) {
            rawData[v] = c;
        }
    }

    private void setAsIs(String s, int offset) {
        for (int u = 0, v = offset; u < s.length(); u++, v++) {
            rawData[v] = s.charAt(u);
        }
    }

    protected boolean checkEqual(int offset, int count, FieldValidateHandler handler, String value) {
        boolean fault = false;
        if (! getAbc(offset, count).equals(value)) {
            handler.error(FixError.Detail
                .builder()
                .offset(offset)
                .length(count)
                .value(getAbc(offset, count))
                .code(ValidateError.NotEqual)
                .build());
            return true;
        }
        return fault;
    }

    protected void testDigit(int offset, int count) {
        for (int u = offset, v = 0; v < count; u++, v++) {
            char c = rawData[u];
            if (!('0' <= c && c <= '9')) {
                throw new FixError.NotDigitException(c, u + 1);
            }
        }
    }

    protected boolean checkLatin(String name, int offset, int count, FieldValidateHandler handler) {
        boolean fault = false;
        for (int u = offset, v = 0; v < count; u++, v++) {
            char c = rawData[u];
            if (!(32 <= c && c < 127) && !(160 <= c && c <= 255)) {
                handler.error(FixError.Detail
                        .builder()
                        .name(name)
                        .offset(offset)
                        .length(count)
                        .value(getAbc(offset, count))
                        .column(u)
                        .code(ValidateError.NotLatin)
                        .wrong(c)
                        .build());
                if (FAIL_FIRST) return true; else fault = true;
            }
        }
        return fault;
    }

    protected void testAscii(int offset, int count) {
        for (int u = offset, v = 0; v < count; u++, v++) {
            char c = rawData[u];
            if (!(32 <= c && c <= 127)) {
                throw new FixError.NotAsciiException(c, u + 1);
            }
        }
    }

    protected void fill(int offset, int count, String s) {
        if (s.length() == count)
            setAsIs(s, offset);
        else if (s.length() < count) {
            throw new FixError.FieldUnderFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + s.length());
        } else {
            throw new FixError.FieldOverFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + s.length());
        }
    }

    protected void testLatin(int offset, int count) {
        for (int u = offset, v = 0; v < count; u++, v++) {
            int c = rawData[u];
            if (!(32 <= c && c < 127) && !(160 <= c && c <= 255)) {
                throw new FixError.NotLatinException(c, u + 1);
            }
        }
    }

    protected void testValid(int offset, int count) {
        for (int u = offset, v = 0; v < count; u++, v++) {
            char c = rawData[u];
            if (Character.isISOControl(c) || !Character.isDefined(c)) {
                throw new FixError.NotValidException(c, u + 1);
            }
        }
    }

    private static String ltrunc(String s, int t) {
        int len = s.length();
        return (len > t) ? s.substring(len-t) : s;
    }

    protected boolean checkValid(String name, int offset, int count, FieldValidateHandler handler) {
        boolean fault = false;
        for (int u = offset, v = 0; v < count; u++, v++) {
            char c = rawData[u];
            if (!Character.isDefined(c) || Character.isISOControl(c)) {
                handler.error(FixError.Detail
                        .builder()
                        .name(name)
                        .offset(offset)
                        .length(count)
                        .value(getAbc(offset, count))
                        .column(u)
                        .code(ValidateError.NotValid)
                        .wrong(c)
                        .build());
                if (FAIL_FIRST) return true; else fault = true;
            }
        }
        return fault;
    }

    protected static void testDigitBlank(String value) {
        if (value == null) return;
        char[] raw = value.toCharArray();
        if (raw[0] == ' ') {
            for (int u = 1; u < raw.length; u++) {
                char c = raw[u];
                if (c != ' ') {
                    throw new FixError.NotBlankException(c, u + 1);
                }
            }
        } else {
            for (int u = 0; u < raw.length; u++) {
                char c = raw[u];
                if (!('0' <= c && c <= '9')) {
                    throw new FixError.NotDigitException(c, u + 1);
                }
            }
        }
    }

    protected void testDigitBlank(int offset, int count) {
        char c = rawData[offset];
        if (c == ' ') {
            for (int u = offset + 1, v = 1; v < count; u++, v++) {
                if (rawData[u] != ' ') {
                    throw new FixError.NotBlankException(c, u + 1);
                }
            }
        } else {
            for (int u = offset, v = 0; v < count; u++, v++) {
                c = rawData[u];
                if (!('0' <= c && c <= '9')) {
                    throw new FixError.NotDigitException(c, u + 1);
                }
            }
        }
    }


    protected String dump(int offset, int count) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < count; k++) {
            char c = rawData[offset + k];
            if (c <= 32) {
                c = (char) (0x2400 + c);
            } else if (c == 127) {
                c = '\u2421'; // delete
            }
            sb.append(c);
        }
        return sb.toString();
    }

    protected void setAbc(String s, int offset, int count) {
        if (s == null) {
            throw new FixError.FieldUnderFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + " null");
        } else if (s.length() == count)
            setAsIs(s, offset);
        else if (s.length() < count) {
            throw new FixError.FieldUnderFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + s.length());
        } else  {
            throw new FixError.FieldOverFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + s.length());
        }
    }

    protected void setAbc(String s, int offset, int count, OverflowAction overflowAction, UnderflowAction underflowAction, char pad, char init) {
        if (s == null) {
            if (underflowAction == UnderflowAction.Error)
                throw new FixError.FieldUnderFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + " null");
            fillChar(offset, count, init);
        } else if (s.length() == count)
            setAsIs(s, offset);
        else if (s.length() < count) {
            switch (underflowAction) {
                case PadR:
                    padToRight(s, offset, count, pad);
                    break;
                case PadL:
                    padToLeft(s, offset, count, pad);
                    break;
                case Error:
                    throw new FixError.FieldUnderFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + s.length());
            }
        } else switch (overflowAction) {
            case TruncR:
                truncRight(s, offset, count);
                break;
            case TruncL:
                truncLeft(s, offset, count);
                break;
            case Error:
                throw new FixError.FieldOverFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + s.length());
        }
    }

    protected void setNum(String s, int offset, int count, OverflowAction ovfl, UnderflowAction unfl, char fill) {
        if (s == null) {
            if (unfl == UnderflowAction.Error)
                throw new FixError.FieldUnderFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + " null");
            fillChar(offset, count, fill);
        } else if (s.length() == count)
            setAsIs(s, offset);
        else if (s.length() < count) {
            switch (unfl) {
                case PadR:
                    padToRight(s, offset, count, '0');
                    break;
                case PadL:
                    padToLeft(s, offset, count, '0');
                    break;
                case Error:
                    throw new FixError.FieldUnderFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + s.length());
            }
        } else switch (ovfl) {
            case TruncR:
                truncRight(s, offset, count);
                break;
            case TruncL:
                truncLeft(s, offset, count);
                break;
            case Error:
                throw new FixError.FieldOverFlowException(FIELD_AT + offset + EXPECTED + count + CHARS_FOUND + s.length());
        }
    }

    protected boolean checkArray(String name, int offset, int count, FieldValidateHandler handler, String[] domain) {
        if (Arrays.binarySearch(domain, getAbc(offset, count)) < 0) {
            handler.error(FixError.Detail
                    .builder()
                    .name(name)
                    .offset(offset)
                    .length(count)
                    .value(getAbc(offset, count))
                    .code(ValidateError.NotDomain)
                    .build());
            return true;
        }
        return false;
    }

    protected boolean checkRegex(String name, int offset, int count, FieldValidateHandler handler, Pattern pattern) {
        Matcher matcher = pattern.matcher(getAbc(offset, count));
        if (!matcher.matches()) {
            handler.error(FixError.Detail
                    .builder()
                    .name(name)
                    .offset(offset)
                    .length(count)
                    .value(getAbc(offset, count))
                    .code(ValidateError.NotMatch)
                    .build());
            return true;
        }
        return false;
    }

    protected void testArray(int offset, int count, String[] domain) {
        String value = getAbc(offset, count);
        if (Arrays.binarySearch(domain, value) < 0)
            throw new FixError.NotDomainException(offset + 1, value);
    }

    protected void testRegex(int offset, int count, Pattern pattern) {
        String value = getAbc(offset, count);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
            throw new FixError.NotMatchesException(offset + 1, value);
    }

    /**
     * Returns the char value at the specified index.
     * @param k the index of the char value.
     * @return  the char value at the specified index of this string. The first char value is at index 1.
     * @throws IndexOutOfBoundsException if the index argument is not between 1 and the length of this daraRecord.
     */
    public char charAt(int k) throws IndexOutOfBoundsException {
        if (k<1 || k>rawData.length)
            throw new IndexOutOfBoundsException(String.valueOf(k));
        return rawData[k-1];
    }
}
