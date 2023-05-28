package io.github.epi155.recfm.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.epi155.recfm.java.FixError.*;

class SetterException extends RuntimeException {
    private final String message;

    SetterException(String value) {
        super();
        Info info = arrangeStack();
        this.message = String.format("%s.%s, offending value \"%s\"", info.name, info.method, explainString(value));

    }

    public SetterException(String value, int offset) {
        Info info = arrangeStack();
        this.message = String.format("%s.%s, offending value \"%s\" @%d+%d", info.name, info.method, explainString(value), offset+RECORD_BASE, value.length());
    }

    SetterException(char ic, int ko, int kp) {
        super();    // getter
        Info info = arrangeStack();
        this.message = String.format("%s.%s, offending char %s @%d^%d", info.name, info.method, explainChar(ic), ko, kp-ko+RECORD_BASE);
    }
    SetterException(char ic, int kp) {
        super();    // setter
        Info info = arrangeStack();
        this.message = String.format("%s.%s, offending char %s ^%d", info.name, info.method, explainChar(ic), kp+RECORD_BASE);
    }

    private Info arrangeStack() {
        fillInStackTrace();
        @SuppressWarnings("Convert2Diamond")
        List<StackTraceElement> stack = new ArrayList<StackTraceElement>(Arrays.asList(getStackTrace()));
        StackTraceElement ste;
        String method;
        String systemPackage = this.getClass().getPackage().getName();
        do {
            ste = stack.remove(0);
            method = ste.getMethodName();
            if (!ste.getClassName().startsWith(systemPackage)) break;
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
