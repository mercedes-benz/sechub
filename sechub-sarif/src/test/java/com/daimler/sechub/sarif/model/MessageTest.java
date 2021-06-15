package com.daimler.sechub.sarif.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.daimler.sechub.test.PojoTester;

class MessageTest {

    @Test
    void value_is_null() {
        /* prepare */
        Message message = new Message(null);

        /* execute */
        String text = message.getText();

        /* test */
        assertEquals(text, null);
    }

    @Test
    void value_is_not_null() {
        /* prepare */
        Message message = new Message("Just a test message.");

        /* execute */
        String text = message.getText();

        /* test */
        assertEquals(text, "Just a test message.");
    }

    @Test
    void test_setter() {
        /* prepare */
        Message message = new Message();

        /* execute + test */
        PojoTester.testSetterAndGetter(message);
    }

}
