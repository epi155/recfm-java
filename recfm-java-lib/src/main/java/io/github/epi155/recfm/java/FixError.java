package io.github.epi155.recfm.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Collector of exceptions that can occur in the management of fixed-path records
 */
public class FixError {
    /**
     * sets the behavior in case of multiple errors on the same field: report the first error or all errors
     */
    public static volatile boolean FAIL_FIRST = false;

    private FixError() {
    }

    /**
     * Exception thrown if the value of a field is larger than the one expected by the structure
     */
    public static class FieldOverFlowException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public FieldOverFlowException(String s) {
            super(s);
        }
    }

    /**
     * Exception thrown if the value of a field is smaller than the one expected by the structure
     */
    public static class FieldUnderFlowException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public FieldUnderFlowException(String s) {
            super(s);
        }
    }

    /**
     * Exception thrown if the value of the structure is larger than expected
     */
    public static class RecordOverflowException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public RecordOverflowException(String s) {
            super(s);
        }
    }

    /**
     * Exception thrown if the value of the structure is smaller than expected
     */
    public static class RecordUnderflowException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public RecordUnderflowException(String s) {
            super(s);
        }
    }

    public static class NotAsciiException extends SetterException {
        public NotAsciiException(char c, int u) {
            super(c, u);
        }
    }

    private static class SetterException extends RuntimeException {
        private final String message;

        SetterException(String value) {
            super();
            Info info = arrangeStack();
            this.message = String.format("%s.%s, offending value %s", info.name, info.method, value);

        }

        public SetterException(String value, int offset) {
            Info info = arrangeStack();
            this.message = String.format("%s.%s, offending value %s @%d", info.name, info.method, value, offset);
        }

        SetterException(int ic, int kp) {
            super();
            Info info = arrangeStack();
            this.message = String.format("%s.%s, offending char U+%04x @+%d", info.name, info.method, ic, kp);
        }

        private Info arrangeStack() {
            fillInStackTrace();
            List<StackTraceElement> stack = new ArrayList<>(Arrays.asList(getStackTrace()));
            StackTraceElement ste;
            String method;
            do {
                ste = stack.remove(0);
                method = ste.getMethodName();
            } while ((!method.startsWith("get")) && (!method.startsWith("set")));
            setStackTrace(stack.toArray(new StackTraceElement[0]));
            return new Info(ste.getClassName(), method);
        }

        @Override
        public String getMessage() {
            return message;
        }

    }

    public static class NotLatinException extends SetterException {
        public NotLatinException(int c, int u) {
            super(c, u);
        }
    }

    public static class NotValidException extends SetterException {
        public NotValidException(char c, int u) {
            super(c, u);
        }
    }

    public static class NotDigitException extends SetterException {
        public NotDigitException(char c, int u) {
            super(c, u);
        }
    }

    public static class NotBlankException extends SetterException {
        public NotBlankException(char c, int u) {
            super(c, u);
        }
    }

    public static class NotDomainException extends SetterException {
        public NotDomainException(String value) {
            super(value);
        }

        public NotDomainException(int offset, String value) {
            super(value, offset);
        }
    }

    public static class NotMatchesException extends SetterException {
        public NotMatchesException(String value) {
            super(value);
        }

        public NotMatchesException(int offset, String value) {
            super(value, offset);
        }
    }

    private static class Info {
        private final String method;
        private final String name;

        public Info(String className, String method) {
            this.name = className;
            this.method = method;
        }
    }

    static class Detail implements FieldValidateError {

        private static final int RECORD_BASE = 1;
        private String name;
        private int offset;
        private int length;
        private String value;
        private Integer column;
        private ValidateError code;
        private Character wrong;
        private Detail() {
        }

        static Builder builder() {
            return new Builder();
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public int offset() {
            return this.offset + RECORD_BASE;
        }

        @Override
        public int length() {
            return this.length;
        }

        @Override
        public String value() {
            return this.value;
        }

        @Override
        public Integer column() {
            return this.column + RECORD_BASE;
        }

        @Override
        public ValidateError code() {
            return this.code;
        }

        @Override
        public Character wrong() {
            return this.wrong;
        }

        @Override
        public String message() {
            if (wrong != null) {
                String charName = Character.getName(wrong);
                int position = column - offset + RECORD_BASE;
                if (charName == null) {
                    return String.format("%d^(U+%04X) [unassigned char] %s", position, (int) wrong, code.name());
                }
                Character.UnicodeBlock block = Character.UnicodeBlock.of(wrong);
                if (Character.isISOControl(wrong) ||
                    block == null ||
                    block == Character.UnicodeBlock.SPECIALS) {
                    return String.format("%d^(U+%04X) [%s] %s", position, (int) wrong, charName, code.name());
                } else {
                    return String.format("%d^'%c' [%s] %s", position, wrong, charName, code.name());
                }
            }
            if (value != null) {
                String sanitizeValue = sanitizeValue();
                return String.format("\"%s\" %s", sanitizeValue, code.name());
            }
            return null;
        }

        private String sanitizeValue() {
            StringBuilder sb = new StringBuilder();
            char[] ca = value.toCharArray();
            for (char c : ca) {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
                if (Character.isISOControl(c) ||
                    !Character.isDefined(c) ||
                    block == null ||
                    block == Character.UnicodeBlock.SPECIALS) {
                    sb.append(String.format("(U+%04X)", (int) c));
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        static class Builder {
            private String name;
            private int offset;
            private int length;
            private String value;
            private Integer column;
            private ValidateError code;
            private Character wrong;
            private Builder() {
            }

            Builder name(String name) {
                this.name = name;
                return this;
            }

            Builder offset(int offset) {
                this.offset = offset;
                return this;
            }

            Builder length(int length) {
                this.length = length;
                return this;
            }

            Builder value(String value) {
                this.value = value;
                return this;
            }

            Builder column(int column) {
                this.column = column;
                return this;
            }

            Builder code(ValidateError code) {
                this.code = code;
                return this;
            }

            Builder wrong(char character) {
                this.wrong = character;
                return this;
            }

            Detail build() {
                Detail ei = new Detail();
                ei.name = name;
                ei.offset = offset;
                ei.length = length;
                ei.value = value;
                ei.column = column;
                ei.code = code;
                ei.wrong = wrong;
                return ei;
            }
        }
    }
}
