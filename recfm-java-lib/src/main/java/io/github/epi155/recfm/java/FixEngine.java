package io.github.epi155.recfm.java;

import java.nio.CharBuffer;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.epi155.recfm.java.FixError.RECORD_BASE;
import static io.github.epi155.recfm.java.FixError.failFirst;

abstract class FixEngine {
    private static final String FIELD_AT = "Field @";
    private static final String EXPECTED = " expected ";
    private static final String CHARS_FOUND = " chars, found ";
    private static final String CHARS_FOUND_NULL = " chars, found [NULL]";
    private static final String RECORD_LENGTH = "Record length ";
    /**
     * record store area
     */
    protected final char[] rawData;

    /**
     * Raw constructor
     *
     * @param length record length
     */
    protected FixEngine(int length) {
        this.rawData = new char[length];
    }

    /**
     * Copy constructor
     *
     * @param c              char array source
     * @param lrec           record length to be copied
     * @param overflowError  overflow behaviour
     * @param underflowError underflow behaviour
     */
    protected FixEngine(char[] c, int lrec, boolean overflowError, boolean underflowError) {
        if (c.length == lrec) {
            rawData = c;
        } else if (c.length > lrec) {
            if (overflowError)
                throw new RecordOverflowException(RECORD_LENGTH + c.length + EXPECTED + lrec);
            rawData = Arrays.copyOfRange(c, 0, lrec);
        } else {
            if (underflowError)
                throw new RecordUnderflowException(RECORD_LENGTH + c.length + EXPECTED + lrec);
            this.rawData = new char[lrec];
            initialize();
            System.arraycopy(c, 0, rawData, 0, c.length);
        }
    }

    /**
     * Check that the supplied string is of digits only
     *
     * @param value string to be checked
     * @throws NotDigitException when check fails
     */
    protected static void testDigit(String value) { // setter
        if (value == null) return;
        char[] raw = value.toCharArray();
        for (int u = 0; u < raw.length; u++) {
            char c = raw[u];
            if (!('0' <= c && c <= '9')) {
                throw new NotDigitException(c, u);
            }
        }
    }

    /**
     * Check that the supplied string is of ascii char only
     *
     * @param value string to be checked
     * @throws NotAsciiException when check fails
     */
    protected static void testAscii(String value) { // setter
        if (value == null) return;
        char[] raw = value.toCharArray();
        for (int u = 0; u < raw.length; u++) {
            char c = raw[u];
            if (!(32 <= c && c < 127)) {
                throw new NotAsciiException(c, u);
            }
        }
    }

    /**
     * Check that the supplied string is of latin1 char only
     *
     * @param value string to be checked
     * @throws NotLatinException when check fails
     */
    protected static void testLatin(String value) { // setter
        if (value == null) return;
        char[] raw = value.toCharArray();
        for (int u = 0; u < raw.length; u++) {
            char c = raw[u];
            if (!(32 <= c && c < 127) && !(160 <= c && c <= 255)) {
                throw new NotLatinException(c, u);
            }
        }
    }

    /**
     * Check that the supplied string is of valid UTF-8 char only
     *
     * @param value string to be checked
     * @throws NotValidException when check fails
     */
    protected static void testValid(String value) { // setter
        if (value == null) return;
        char[] raw = value.toCharArray();
        for (int u = 0; u < raw.length; u++) {
            char c = raw[u];
            if (Character.isISOControl(c) || !Character.isDefined(c)) {
                throw new NotValidException(c, u);
            }
        }
    }

    /**
     * Check that the supplied string is all digits or al SPACE char only
     *
     * @param value string to be checked
     * @throws NotBlankException when first char is space but not all SPACES
     * @throws NotDigitException when not all digits
     */
    protected static void testDigitBlank(/*NotNull*/String value) {    // setter
        char[] raw = value.toCharArray();
        char c = raw[0];
        if (c == ' ') {
            for (int u = 1; u < raw.length; u++) {
                char cu = raw[u];
                if (cu != ' ') {
                    throw new NotBlankException(cu, u);
                }
            }
        } else if ('0' <= c && c <= '9'){
            for (int u = 0; u < raw.length; u++) {
                char cu = raw[u];
                if (!('0' <= cu && cu <= '9')) {
                    throw new NotDigitException(c, u);
                }
            }
        } else {
            throw new NotDigitBlankException(c, 0);
        }
    }

    /**
     * Check that the supplied string is in permitted domain
     *
     * @param value  string to be checked
     * @param domain string array with permitted domain
     * @throws NotDomainException when check fails
     */
    protected static void testArray(String value, String[] domain) {    // setter
        if (value == null) return;
        if (Arrays.binarySearch(domain, value) < 0)
            throw new NotDomainException(value);
    }

    /**
     * Check that the supplied string matches regular expression
     *
     * @param value   string to be checked
     * @param pattern regular expression pattern
     * @throws NotMatchesException when check fails
     */
    protected static void testRegex(String value, Pattern pattern) {    // setter
        if (value == null) return;
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
            throw new NotMatchesException(value);
    }

    /**
     * String value normalizer
     *
     * @param s               original value
     * @param overflowAction  overflow behaviour
     * @param underflowAction underflow behaviour
     * @param pad             padding char
     * @param init            initialize char
     * @param offset          field offset
     * @param length          field length
     * @return normalized value
     */
    protected static String normalize(String s,
                                      OverflowAction overflowAction,
                                      UnderflowAction underflowAction,
                                      char pad, char init,
                                      int offset, int length) {
        if (s == null) {
            if (underflowAction == UnderflowAction.Error)
                throw new FieldUnderFlowException(FIELD_AT + (offset+RECORD_BASE) + EXPECTED + length + CHARS_FOUND_NULL);
            return fill(length, init);
        } else if (s.length() == length)
            return s;
        else if (s.length() < length) {
            switch (underflowAction) {
                case PadR:
                    return rpad(s, length, pad);
                case PadL:
                    return lpad(s, length, pad);
                case Error:
                    throw new FieldUnderFlowException(FIELD_AT + (offset+RECORD_BASE) + EXPECTED + length + CHARS_FOUND + s.length());
            }
        } else /* s.length() > length */switch (overflowAction) {
            case TruncR:
                return rtrunc(s, length);
            case TruncL:
                return ltrunc(s, length);
            case Error:
                throw new FieldOverFlowException(FIELD_AT + (offset+RECORD_BASE) + EXPECTED + length + CHARS_FOUND + s.length());
        }
        return null; // dead branch (?)
    }

    private static String fill(int t, char pad) {
        return CharBuffer.allocate(t).toString().replace('\0', pad);
    }

    private static String rpad(String s, int w, char pad) {
        int len = s.length();
        return s + CharBuffer.allocate(w - len).toString().replace('\0', pad);
    }

    /**
     * Number formatting factory
     *
     * @param digits number digit length
     * @return {@link NumberFormat} instance
     */
    protected static NumberFormat pic9(int digits) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(digits);
        nf.setGroupingUsed(false);
        return nf;
    }

    private static String ltrunc(String s, int w) {
        int len = s.length();
        return s.substring(len - w);
    }

    private static String lpad(String s, int t, char pad) {
        int len = s.length();
        return CharBuffer.allocate(t - len).toString().replace('\0', pad) + s;
    }

    private static String rtrunc(String s, int w) {
        return s.substring(0, w);
    }

    /**
     * Initialize the fields of the record
     */
    protected abstract void initialize();

    /**
     * Alphanumeric getter
     *
     * @param offset field offset
     * @param length field length
     * @return field value
     */
    protected String getAbc(int offset, int length) {
        return new String(rawData, offset, length);
    }

    /**
     * Check that the record field is of digits only
     *
     * @param offset field offset
     * @param length field length
     * @throws NotDigitException when check fails
     */
    protected void testDigit(int offset, int length) {  // getter
        for (int u = offset, v = 0; v < length; u++, v++) {
            char c = rawData[u];
            if (!('0' <= c && c <= '9')) {
                throw new NotDigitException(c, offset, u);
            }
        }
    }

    /**
     * Check that the record field is of ascii char only
     *
     * @param offset field offset
     * @param length field length
     * @throws NotAsciiException when check fails
     */
    protected void testAscii(int offset, int length) {  // getter
        for (int u = offset, v = 0; v < length; u++, v++) {
            char c = rawData[u];
            if (!(32 <= c && c <= 127)) {
                throw new NotAsciiException(c, offset, u);
            }
        }
    }

    private void fillChar(int offset, int count, char fill) {
        for (int u = 0, v = offset; u < count; u++, v++) {
            rawData[v] = fill;
        }
    }

    /**
     * Check that the record field is of latin1 char only
     *
     * @param offset field offset
     * @param length field length
     * @throws NotAsciiException when check fails
     */
    protected void testLatin(int offset, int length) {  // getter
        for (int u = offset, v = 0; v < length; u++, v++) {
            char c = rawData[u];
            if (!(32 <= c && c < 127) && !(160 <= c && c <= 255)) {
                throw new NotLatinException(c, offset, u);
            }
        }
    }

    /**
     * Check that the record field is of valid UTF-8 char only
     *
     * @param offset field offset
     * @param length field length
     * @throws NotValidException when check fails
     */
    protected void testValid(int offset, int length) {  // getter
        for (int u = offset, v = 0; v < length; u++, v++) {
            char c = rawData[u];
            if (Character.isISOControl(c) || !Character.isDefined(c)) {
                throw new NotValidException(c, offset, u);
            }
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

    /**
     * Check that the record field is all digits or al SPACE char only
     *
     * @param offset field offset
     * @param length field length
     * @throws NotBlankException when first char is space but not all SPACES
     * @throws NotDigitException when not all digits
     */
    protected void testDigitBlank(int offset, int length) { // getter
        char c = rawData[offset];
        if (c == ' ') {
            for (int u = offset + 1, v = 1; v < length; u++, v++) {
                char cu = rawData[u];
                if (cu != ' ') {
                    throw new NotBlankException(cu, offset, u);
                }
            }
        } else if ('0' <= c && c <= '9') {
            for (int u = offset, v = 0; v < length; u++, v++) {
                char cu = rawData[u];
                if (!('0' <= cu && cu <= '9')) {
                    throw new NotDigitException(cu, offset, u);
                }
            }
        } else {
            throw new NotDigitBlankException(c, offset, offset);
        }
    }

    /**
     * Check that the record field is in permitted domain
     *
     * @param offset field offset
     * @param length field length
     * @param domain string array with permitted domain
     * @throws NotDomainException when check fails
     */
    protected void testArray(int offset, int length, String[] domain) {  // getter
        String value = getAbc(offset, length);
        if (Arrays.binarySearch(domain, value) < 0)
            throw new NotDomainException(offset, value);
    }

    /**
     * Check that the record field matches regular expression
     *
     * @param offset  field offset
     * @param length  field length
     * @param pattern regular expression pattern
     * @throws NotMatchesException when check fails
     */
    protected void testRegex(int offset, int length, Pattern pattern) {  // getter
        String value = getAbc(offset, length);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
            throw new NotMatchesException(offset, value);
    }

    /**
     * Record filler
     *
     * @param offset record (field) offset
     * @param length record (field) length
     * @param c      fill char
     */
    protected void fill(int offset, int length, char c) {
        for (int u = offset, v = 0; v < length; u++, v++) {
            rawData[u] = c;
        }
    }

    /**
     * Encode current record to string
     *
     * @return string serialization of this record
     */
    public String encode() {
        return new String(rawData);
    }

    /**
     * Validate all fields
     *
     * @param handler error handler
     * @return <b>true</b> if there is an error, <b>false</b> if there are no errors
     */
    public abstract boolean validateFails(FieldValidateHandler handler);

    /**
     * Validate fields marked with <i>audit</i>: <b>true</b>
     *
     * @param handler error handler
     * @return <b>true</b> if there is an error, <b>false</b> if there are no errors
     */
    public abstract boolean auditFails(FieldValidateHandler handler);

    /**
     * Check that the record field is of digits only
     *
     * @param name    field name
     * @param offset  field offset
     * @param length  field length
     * @param handler error handler
     * @return <b>true</b> if there is an error, <b>false</b> if there are no errors
     */
    protected boolean checkDigit(String name, int offset, int length, FieldValidateHandler handler) {
        boolean fault = false;
        for (int u = offset, v = 0; v < length; u++, v++) {
            char c = rawData[u];
            if (!('0' <= c && c <= '9')) {
                handler.error(Detail
                    .builder()
                    .name(name)
                    .offset(offset)
                    .length(length)
                    .value(getAbc(offset, length))
                    .column(u)
                    .code(ValidateError.NotNumber)
                    .wrong(c)
                    .build());
                if (failFirst) return true;
                else fault = true;
            }
        }
        return fault;
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

    /**
     * Check that the record field is of digits only or SPACE only
     *
     * @param name    field name
     * @param offset  field offset
     * @param length  field length
     * @param handler error handler
     * @return <b>true</b> if there is an error, <b>false</b> if there are no errors
     */
    protected boolean checkDigitBlank(String name, int offset, int length, FieldValidateHandler handler) {
        char c = rawData[offset];
        if (c == ' ') {
            return checkAllSpace(name, offset, length, handler);
        } else if ('0' <= c && c <= '9') {
            return checkAllDigit(name, offset, length, handler);
        } else {
            handler.error(Detail
                    .builder()
                    .name(name)
                    .offset(offset)
                    .length(length)
                    .value(getAbc(offset, length))
                    .column(offset)
                    .code(ValidateError.NotDigitBlank)  // ??
                    .wrong(c)
                    .build());
            return true;
        }
    }

    private boolean checkAllSpace(String name, int offset, int length, FieldValidateHandler handler) {
        boolean fault = false;
        for (int u = offset + 1, v = 1; v < length; u++, v++) {
            char c = rawData[u];
            if (c != ' ') {
                handler.error(Detail
                    .builder()
                    .name(name)
                    .offset(offset)
                    .length(length)
                    .value(getAbc(offset, length))
                    .column(u)
                    .code(ValidateError.NotBlank)  // ??
                    .wrong(c)
                    .build());
                if (failFirst) return true;
                else fault = true;
            }
        }
        return fault;
    }

    private boolean checkAllDigit(String name, int offset, int length, FieldValidateHandler handler) {
        boolean fault = false;
        for (int u = offset + 1, v = 1; v < length; u++, v++) {
            char c = rawData[u];
            if (!('0' <= c && c <= '9')) {
                handler.error(Detail
                    .builder()
                    .name(name)
                    .offset(offset)
                    .length(length)
                    .value(getAbc(offset, length))
                    .column(u)
                    .code(ValidateError.NotNumber)
                    .wrong(c)
                    .build());
                if (failFirst) return true;
                else fault = true;
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

    /**
     * set validated/normalized field value
     * @param s         field value
     * @param offset    field offset
     */
    protected void setAsIs(String s, int offset) {
        for (int u = 0, v = offset; u < s.length(); u++, v++) {
            rawData[v] = s.charAt(u);
        }
    }

    /**
     * set validated/normalized domain field value
     * @param s         field value
     * @param offset    field offset
     * @param init      field default value on null field value
     */
    protected void setDom(String s, int offset, String init) {
        if (s == null) {
            setAsIs(init, offset);
        } else {
            setAsIs(s, offset);
        }
    }
    /**
     * Check that the record field is of ascii char only
     *
     * @param name    field name
     * @param offset  field offset
     * @param length  field length
     * @param handler error handler
     * @return <b>true</b> if there is an error, <b>false</b> if there are no errors
     */
    protected boolean checkAscii(String name, int offset, int length, FieldValidateHandler handler) {
        boolean fault = false;
        for (int u = offset, v = 0; v < length; u++, v++) {
            char c = rawData[u];
            if (!(32 <= c && c < 127)) {
                handler.error(Detail
                    .builder()
                    .name(name)
                    .offset(offset)
                    .length(length)
                    .value(getAbc(offset, length))
                    .column(u)
                    .code(ValidateError.NotAscii)
                    .wrong(c)
                    .build());
                if (failFirst) return true;
                else fault = true;
            }
        }
        return fault;
    }

    /**
     * Check that the record field matches expected value
     *
     * @param offset  field offset
     * @param length  field length
     * @param handler error handler
     * @param value   expected value
     * @return <b>true</b> if there is an error, <b>false</b> if there are no errors
     */
    protected boolean checkEqual(int offset, int length, FieldValidateHandler handler, String value) {
        boolean fault = false;
        if (!getAbc(offset, length).equals(value)) {
            handler.error(Detail
                .builder()
                .offset(offset)
                .length(length)
                .value(getAbc(offset, length))
                .code(ValidateError.NotEqual)
                .build());
            return true;
        }
        return fault;
    }

    /**
     * Validate alphanumeric latin1 field type
     *
     * @param name    field name
     * @param offset  field offset
     * @param length  field length
     * @param handler error handler
     * @return <b>true</b> if there is an error, <b>false</b> if there are no errors
     */
    protected boolean checkLatin(String name, int offset, int length, FieldValidateHandler handler) {
        boolean fault = false;
        for (int u = offset, v = 0; v < length; u++, v++) {
            char c = rawData[u];
            if (!(32 <= c && c < 127) && !(160 <= c && c <= 255)) {
                handler.error(Detail
                    .builder()
                    .name(name)
                    .offset(offset)
                    .length(length)
                    .value(getAbc(offset, length))
                    .column(u)
                    .code(ValidateError.NotLatin)
                    .wrong(c)
                    .build());
                if (failFirst) return true;
                else fault = true;
            }
        }
        return fault;
    }

    /**
     * Initialize constant field value
     *
     * @param offset field offset
     * @param length field length
     * @param s      initialize string constant
     */
    protected void fill(int offset, int length, String s) {
        if (s.length() == length)
            setAsIs(s, offset);
        else // dead branch
            if (s.length() < length) {  // buffer underflow protection
                throw new FieldUnderFlowException(FIELD_AT + (offset + RECORD_BASE) + EXPECTED + length + CHARS_FOUND + s.length());
            } else {                    // buffer overflow protection
                throw new FieldOverFlowException(FIELD_AT + (offset + RECORD_BASE) + EXPECTED + length + CHARS_FOUND + s.length());
            }
    }

    /**
     * Validate an alphanumeric UTF-8 field
     *
     * @param name    field name
     * @param offset  field offset
     * @param length  field length
     * @param handler error handler
     * @return <b>true</b> if there is an error, <b>false</b> if there are no errors
     */
    protected boolean checkValid(String name, int offset, int length, FieldValidateHandler handler) {
        boolean fault = false;
        for (int u = offset, v = 0; v < length; u++, v++) {
            char c = rawData[u];
            if (!Character.isDefined(c) || Character.isISOControl(c)) {
                handler.error(Detail
                    .builder()
                    .name(name)
                    .offset(offset)
                    .length(length)
                    .value(getAbc(offset, length))
                    .column(u)
                    .code(ValidateError.NotValid)
                    .wrong(c)
                    .build());
                if (failFirst) return true;
                else fault = true;
            }
        }
        return fault;
    }

    /**
     * Dump field value
     *
     * @param offset field offset
     * @param length field length
     * @return field dump value
     */
    protected String dump(int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < length; k++) {
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

    /**
     * Set alphanumeric field value
     *
     * @param s               field value
     * @param offset          field offset
     * @param length          field length
     * @param overflowAction  overflow behaviour
     * @param underflowAction underflow behaviour
     * @param pad             padding char
     * @param init            initialize char
     */
    protected void setAbc(String s, int offset, int length, OverflowAction overflowAction, UnderflowAction underflowAction, char pad, char init) {
        if (s == null) {
            if (underflowAction == UnderflowAction.Error)
                throw new FieldUnderFlowException(FIELD_AT + (offset+RECORD_BASE) + EXPECTED + length + CHARS_FOUND_NULL);
            fillChar(offset, length, init);
        } else if (s.length() == length)
            setAsIs(s, offset);
        else if (s.length() < length) {
            switch (underflowAction) {
                case PadR:
                    padToRight(s, offset, length, pad);
                    break;
                case Error:
                    throw new FieldUnderFlowException(FIELD_AT + (offset+RECORD_BASE) + EXPECTED + length + CHARS_FOUND + s.length());
                case PadL:  // used by relaxed custom fields
                    padToLeft(s, offset, length, pad);
                    break;
            }
        } else switch (overflowAction) {
            case TruncR:
                truncRight(s, offset, length);
                break;
            case Error:
                throw new FieldOverFlowException(FIELD_AT + (offset+RECORD_BASE) + EXPECTED + length + CHARS_FOUND + s.length());
            case TruncL:    // used by relaxed custom fields
                truncLeft(s, offset, length);
                break;
        }
    }

    /**
     * Set numeric field value
     *
     * @param s               field value
     * @param offset          field offset
     * @param length          field length
     * @param overflowAction  overflow behaviour
     * @param underflowAction underflow behaviour
     */
    protected void setNum(String s, int offset, int length, OverflowAction overflowAction, UnderflowAction underflowAction) {
        if (s == null) {
            if (underflowAction == UnderflowAction.Error)
                throw new FieldUnderFlowException(FIELD_AT + (offset+RECORD_BASE) + EXPECTED + length + CHARS_FOUND_NULL);
            fillChar(offset, length, '0');
        } else if (s.length() == length)
            setAsIs(s, offset);
        else if (s.length() < length) {
            switch (underflowAction) {
                case PadL:
                    padToLeft(s, offset, length, '0');
                    break;
                case Error:
                    throw new FieldUnderFlowException(FIELD_AT + (offset+RECORD_BASE) + EXPECTED + length + CHARS_FOUND + s.length());
                case PadR:  // dead branch
                    padToRight(s, offset, length, '0');
                    break;
            }
        } else switch (overflowAction) {
            case TruncL:
                truncLeft(s, offset, length);
                break;
            case Error:
                throw new FieldOverFlowException(FIELD_AT + (offset+RECORD_BASE) + EXPECTED + length + CHARS_FOUND + s.length());
            case TruncR:    // dead branch
                truncRight(s, offset, length);
                break;
        }
    }

    /**
     * Validate domain field type
     *
     * @param name    field name
     * @param offset  field offset
     * @param length  field length
     * @param handler error handler
     * @param domain  domain values
     * @return <b>true</b> if value not in domain, <b>false</b> if value in domain
     */
    protected boolean checkArray(String name, int offset, int length, FieldValidateHandler handler, String[] domain) {
        if (Arrays.binarySearch(domain, getAbc(offset, length)) < 0) {
            handler.error(Detail
                .builder()
                .name(name)
                .offset(offset)
                .length(length)
                .value(getAbc(offset, length))
                .code(ValidateError.NotDomain)
                .build());
            return true;
        }
        return false;
    }

    /**
     * Validate regex field type
     *
     * @param name    field name
     * @param offset  field offset
     * @param length  field length
     * @param handler error handler
     * @param pattern regular expression pattern
     * @return <b>true</b> if value does not match, <b>false</b> if value matches
     */
    protected boolean checkRegex(String name, int offset, int length, FieldValidateHandler handler, Pattern pattern) {
        Matcher matcher = pattern.matcher(getAbc(offset, length));
        if (!matcher.matches()) {
            handler.error(Detail
                .builder()
                .name(name)
                .offset(offset)
                .length(length)
                .value(getAbc(offset, length))
                .code(ValidateError.NotMatch)
                .build());
            return true;
        }
        return false;
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
