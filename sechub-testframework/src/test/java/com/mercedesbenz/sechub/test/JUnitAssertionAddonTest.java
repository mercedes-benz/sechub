package com.mercedesbenz.sechub.test;

import static org.junit.Assert.*;

import org.junit.Test;

public class JUnitAssertionAddonTest {

    @Test
    public void no_failure_when_expected_exception_okay() {
        JUnitAssertionAddon.assertThrowsExceptionContainingMessage(RuntimeException.class, "i am the message", () -> {
            throw new RuntimeException("i am the message");
        });
    }

    @Test
    public void no_failure_when_expected_exception_of_subtype_okay() {
        JUnitAssertionAddon.assertThrowsExceptionContainingMessage(RuntimeException.class, "i am the message", () -> {
            throw new IllegalStateException("i am the message");
        });
    }

    @Test
    public void failure_when_expected_exception_NOT_of_subtype() {
        try {
            JUnitAssertionAddon.assertThrowsExceptionContainingMessage(IllegalStateException.class, "i am the message", () -> {
                throw new RuntimeException("i am the message");
            });
            fail("no error happend...");
        } catch (AssertionError e) {
            /* okay */
        }
    }

}
