package io.github.epi155.recfm.java;

import static io.github.epi155.recfm.java.FixError.explainChar;

class Detail implements FieldValidateError {

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
        return this.offset + FixError.RECORD_BASE;
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
        return this.column + FixError.RECORD_BASE;
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
            int position = column - offset + FixError.RECORD_BASE;
            return String.format("%d^%s %s", position, explainChar(wrong), code.name());
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
