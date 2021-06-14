package com.daimler.sechub.sarif.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class MessageTest {

    @Test
    public void value_is_null() {
        /* prepare */
        Message message = new Message(null);

        /* execute */
        String text = message.getText();

        /* test */
        assertEquals(text, null);
    }

    @Test
    public void value_is_not_null() {
        /* prepare */
        Message message = new Message("Just a test message.");

        /* execute */
        String text = message.getText();

        /* test */
        assertEquals(text, "Just a test message.");
    }

    @Test
    public void test_setter() {
        /* prepare */
        Message message = new Message();

        /* execute */
        message.setText("Just a test message.");

        /* test */
        assertEquals(message.getText(), "Just a test message.");
    }

}
