package com.utils;
/**
 * Inferrules-specific assertion checking.
 *
 * <p>This may go away in favor of Java language-level assertions.
 */
public class Assertions {
//    An assertion to call when reaching a point that should not be reached.
    public static void UNREACHABLE() {
        throw new UnimplementedError();
    }

    public static void UNREACHABLE(String message) {
        throw new UnimplementedError(message);
    }

}

class UnimplementedError extends Error {
    public UnimplementedError() {
        super();
    }
    public UnimplementedError(String s) {
        super(s);
    }
}