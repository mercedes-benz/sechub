// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MessageTest {

    @Test
    void constructor_with_params_null() {
        /* prepare */
        Message message = new Message(null);

        /* execute */
        String text = message.getText();

        /* test */
        assertEquals(text, null);
    }

    @Test
    void constructor_with_params_not_null() {
        /* prepare */
        Message message = new Message("Just a test message.");

        /* execute */
        String text = message.getText();

        /* test */
        assertEquals(text, "Just a test message.");
    }

    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());
        testBothAreNOTEqual(createExample(), change(createExample(), (message) -> message.setText("other")));
        /* @formatter:on */

    }

    private Message createExample() {
        return new Message();
    }

}
