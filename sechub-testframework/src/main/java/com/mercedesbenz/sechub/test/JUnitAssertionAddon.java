package com.mercedesbenz.sechub.test;

public class JUnitAssertionAddon {

    /**
     * Asserts given executable throws an exception of given type and contains also
     * the given message as a part of the exception message
     *
     * @param clazz               class of expected throwable
     * @param expectedMessagePart a part of the message inside the expected
     *                            throwable
     * @param executable          unit test executable which shall fail
     */
    public static <T extends Throwable> void assertThrowsExceptionContainingMessage(Class<T> clazz, String expectedMessagePart, UnitTestExecutable executable) {
        if (clazz == null) {
            throw new IllegalArgumentException("Testcase corrupt: exception clazz must be defined!");
        }
        boolean exceptionHappend = false;
        try {
            executable.execute();
            exceptionHappend = true;
        } catch (Throwable e) {
            String exceptionMessage = e.getMessage();
            if (!clazz.isAssignableFrom(e.getClass())) {
                throw new AssertionError("Expected a exception of type: " + clazz + "\nbut exception was of type:" + e.getClass(), e);
            }

            if (exceptionMessage == null) {
                if (expectedMessagePart == null) {
                    return;
                }
                failBecauseMessageDiffers(clazz, exceptionMessage, expectedMessagePart);
            } else {
                if (expectedMessagePart == null) {
                    failBecauseMessageDiffers(clazz, exceptionMessage, expectedMessagePart);
                }
                if (!exceptionMessage.contains(expectedMessagePart)) {
                    failBecauseMessageDiffers(clazz, exceptionMessage, expectedMessagePart);
                }
            }
        }
        if (exceptionHappend) {
            throw new AssertionError("Expected a exception of type: " + clazz + ", but no exception happend!");
        }
    }

    private static void failBecauseMessageDiffers(Class<?> clazz, String exceptionMessage, String expectedMessagePart) {
        String prefix = "Found expected type of exception: " + clazz + "\n";
        throw new AssertionError(prefix + "- expected containing: " + expectedMessagePart + "\n- but message was: " + exceptionMessage);
    }

    public interface UnitTestExecutable {

        void execute() throws Throwable;

    }
}
