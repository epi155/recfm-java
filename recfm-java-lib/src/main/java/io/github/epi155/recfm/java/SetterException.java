package io.github.epi155.recfm.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SetterException extends RuntimeException {
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

    private static class Info {
        private final String method;
        private final String name;

        public Info(String className, String method) {
            this.name = className;
            this.method = method;
        }
    }
}
