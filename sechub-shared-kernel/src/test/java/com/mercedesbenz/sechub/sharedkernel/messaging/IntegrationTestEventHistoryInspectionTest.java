// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import static org.junit.Assert.*;

import org.junit.Test;

public class IntegrationTestEventHistoryInspectionTest {

    @Test
    public void fail_when_i1_and_i2_are_nearly_same_but_i2_is_synchronous() {
        /* prepare */
        IntegrationTestEventHistoryInspection i1 = new IntegrationTestEventHistoryInspection();

        i1.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i1.getReceiverClassNames().add("rclass1");
        i1.getReceiverClassNames().add("rclass2");

        IntegrationTestEventHistoryInspection i2 = new IntegrationTestEventHistoryInspection();

        i2.setSynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i2.getReceiverClassNames().add("rclass1");
        i2.getReceiverClassNames().add("rclass2");

        /* execute */
        boolean equals = i1.equals(i2);

        /* test */
        assertFalse(equals);
    }

    @Test
    public void fail_when_i1_and_i2_are_nearly_same_but_i2_has_other_sender_class_than_i2() {
        /* prepare */
        IntegrationTestEventHistoryInspection i1 = new IntegrationTestEventHistoryInspection();

        i1.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i1.getReceiverClassNames().add("rclass1");
        i1.getReceiverClassNames().add("rclass2");

        IntegrationTestEventHistoryInspection i2 = new IntegrationTestEventHistoryInspection();

        i2.setAsynchronousSender("class1-other", MessageID.JOB_CANCELLATION_RUNNING);
        i2.getReceiverClassNames().add("rclass1");
        i2.getReceiverClassNames().add("rclass2");

        /* execute */
        boolean equals = i1.equals(i2);

        /* test */
        assertFalse(equals);
    }

    @Test
    public void fail_when_i1_and_i2_are_nearly_same_but_i2_has_other_event_id_than_i2() {
        /* prepare */
        IntegrationTestEventHistoryInspection i1 = new IntegrationTestEventHistoryInspection();

        i1.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i1.getReceiverClassNames().add("rclass1");
        i1.getReceiverClassNames().add("rclass2");

        IntegrationTestEventHistoryInspection i2 = new IntegrationTestEventHistoryInspection();

        i2.setAsynchronousSender("class1", MessageID.JOB_STARTED);
        i2.getReceiverClassNames().add("rclass1");
        i2.getReceiverClassNames().add("rclass2");

        /* execute */
        boolean equals = i1.equals(i2);

        /* test */
        assertFalse(equals);
    }

    @Test
    public void fail_when_i1_has_one_more_receiver() {
        /* prepare */
        IntegrationTestEventHistoryInspection i1 = new IntegrationTestEventHistoryInspection();

        i1.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i1.getReceiverClassNames().add("rclass1");
        i1.getReceiverClassNames().add("rclass2");
        i1.getReceiverClassNames().add("rclass1");// same content, but another element

        IntegrationTestEventHistoryInspection i2 = new IntegrationTestEventHistoryInspection();

        i2.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i2.getReceiverClassNames().add("rclass1");
        i2.getReceiverClassNames().add("rclass2");

        /* execute */
        boolean equals = i1.equals(i2);

        /* test */
        assertFalse(equals);
    }

    @Test
    public void fail_when_i1_and_i2_have_one_different_receiver() {
        /* prepare */
        IntegrationTestEventHistoryInspection i1 = new IntegrationTestEventHistoryInspection();

        i1.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i1.getReceiverClassNames().add("rclass1");
        i1.getReceiverClassNames().add("rclass2");
        i1.getReceiverClassNames().add("rclass3");

        IntegrationTestEventHistoryInspection i2 = new IntegrationTestEventHistoryInspection();

        i2.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i2.getReceiverClassNames().add("rclass1");
        i2.getReceiverClassNames().add("rclass2");
        i2.getReceiverClassNames().add("rclass4");

        /* execute */
        boolean equals = i1.equals(i2);

        /* test */
        assertFalse(equals);
    }

    @Test
    public void both_empty_must_be_equal() {
        /* prepare */
        IntegrationTestEventHistoryInspection i1 = new IntegrationTestEventHistoryInspection();

        IntegrationTestEventHistoryInspection i2 = new IntegrationTestEventHistoryInspection();

        /* execute */
        boolean equals = i1.equals(i2);

        /* test */
        assertTrue(equals);
    }

    @Test
    public void exact_same_content_must_be_equal() {
        /* prepare */
        IntegrationTestEventHistoryInspection i1 = new IntegrationTestEventHistoryInspection();

        i1.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i1.getReceiverClassNames().add("rclass1");
        i1.getReceiverClassNames().add("rclass2");

        IntegrationTestEventHistoryInspection i2 = new IntegrationTestEventHistoryInspection();

        i2.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i2.getReceiverClassNames().add("rclass1");
        i2.getReceiverClassNames().add("rclass2");

        /* execute */
        boolean equals = i1.equals(i2);

        /* test */
        assertTrue(equals);
    }

    @Test
    public void exact_same_content_must_be_equal_sync_variant() {
        /* prepare */
        IntegrationTestEventHistoryInspection i1 = new IntegrationTestEventHistoryInspection();

        i1.setSynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i1.getReceiverClassNames().add("rclass1");
        i1.getReceiverClassNames().add("rclass2");

        IntegrationTestEventHistoryInspection i2 = new IntegrationTestEventHistoryInspection();

        i2.setSynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i2.getReceiverClassNames().add("rclass1");
        i2.getReceiverClassNames().add("rclass2");

        /* execute */
        boolean equals = i1.equals(i2);

        /* test */
        assertTrue(equals);
    }

    @Test
    public void same_content_but_ordering_differs_must_be_equal() {
        /* prepare */
        IntegrationTestEventHistoryInspection i1 = new IntegrationTestEventHistoryInspection();

        i1.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i1.getReceiverClassNames().add("rclass1");
        i1.getReceiverClassNames().add("rclass2");

        IntegrationTestEventHistoryInspection i2 = new IntegrationTestEventHistoryInspection();

        i2.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i2.getReceiverClassNames().add("rclass2");
        i2.getReceiverClassNames().add("rclass1");

        /* execute */
        boolean equals = i1.equals(i2);

        /* test */
        assertTrue("equals wrong implemented, ordering should not matter!", equals);
    }

    @Test
    public void same_content_with_duplicated__receiver_and_ordering_differs_must_be_equal() {
        /* prepare */
        IntegrationTestEventHistoryInspection i1 = new IntegrationTestEventHistoryInspection();

        i1.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i1.getReceiverClassNames().add("rclass1");
        i1.getReceiverClassNames().add("rclass2");
        i1.getReceiverClassNames().add("rclass1");

        IntegrationTestEventHistoryInspection i2 = new IntegrationTestEventHistoryInspection();

        i2.setAsynchronousSender("class1", MessageID.JOB_CANCELLATION_RUNNING);
        i2.getReceiverClassNames().add("rclass2");
        i2.getReceiverClassNames().add("rclass1");
        i2.getReceiverClassNames().add("rclass1");

        /* execute */
        boolean equals = i1.equals(i2);

        /* test */
        assertTrue("equals wrong implemented, ordering should not matter!", equals);
    }

}
