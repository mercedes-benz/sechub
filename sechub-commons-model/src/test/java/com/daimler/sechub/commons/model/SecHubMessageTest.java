// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

class SecHubMessageTest {

    /* Why we need ordering? so results are always same listed when recreated */
    @Test
    void types_ordering_is_error_warn_than_info_in_a_treeset() {
        /* prepare */

        SecHubMessage info1 = new SecHubMessage(SecHubMessageType.INFO, "info1");
        SecHubMessage error1 = new SecHubMessage(SecHubMessageType.ERROR, "error1");
        SecHubMessage warning1 = new SecHubMessage(SecHubMessageType.WARNING, "error1");

        /* execute + test */
        assertOrderingAsExpected(new TreeSet<>(), new SecHubMessage[] { info1 }, new SecHubMessage[] { info1 });
        assertOrderingAsExpected(new TreeSet<>(), new SecHubMessage[] { info1, error1 }, new SecHubMessage[] { error1, info1 });
        assertOrderingAsExpected(new TreeSet<>(), new SecHubMessage[] { error1, info1 }, new SecHubMessage[] { error1, info1 });
        assertOrderingAsExpected(new TreeSet<>(), new SecHubMessage[] { warning1, info1 }, new SecHubMessage[] { warning1, info1 });
        assertOrderingAsExpected(new TreeSet<>(), new SecHubMessage[] { info1, warning1 }, new SecHubMessage[] { warning1, info1 });

        assertOrderingAsExpected(new TreeSet<>(), new SecHubMessage[] { warning1, error1 }, new SecHubMessage[] { error1, warning1 });
        assertOrderingAsExpected(new TreeSet<>(), new SecHubMessage[] { error1, warning1 }, new SecHubMessage[] { error1, warning1 });
    }

    /* Why we need ordering? so results are always same listed when recreated */
    @Test
    void messages_are_used_for_ordering_but_are_overriden_by_message_type() {
        /* prepare */

        SecHubMessage info1 = new SecHubMessage(SecHubMessageType.INFO, "info1");
        SecHubMessage info2 = new SecHubMessage(SecHubMessageType.INFO, "info2");
        SecHubMessage error3 = new SecHubMessage(SecHubMessageType.ERROR, "error3");

        /* execute + test */
        assertOrderingAsExpected(new TreeSet<>(), new SecHubMessage[] { info1, info2 }, new SecHubMessage[] { info1, info2 });
        assertOrderingAsExpected(new TreeSet<>(), new SecHubMessage[] { info2, info1 }, new SecHubMessage[] { info1, info2 });
        assertOrderingAsExpected(new TreeSet<>(), new SecHubMessage[] { error3, info2, info1 }, new SecHubMessage[] { error3, info1, info2 });
        assertOrderingAsExpected(new TreeSet<>(), new SecHubMessage[] { info2, info1, error3 }, new SecHubMessage[] { error3, info1, info2 });
    }

    private void assertOrderingAsExpected(Set<SecHubMessage> messages, SecHubMessage[] messagesToInject, SecHubMessage[] expectedOrdering) {
        if (messagesToInject.length != expectedOrdering.length) {
            throw new IllegalStateException("wrong implemented testcase array length may not differ!");
        }
        for (SecHubMessage messageToInject : messagesToInject) {
            messages.add(messageToInject);
        }

        Iterator<SecHubMessage> it = messages.iterator();
        for (SecHubMessage messageExpected : expectedOrdering) {
            assertSame(messageExpected, it.next());
        }

    }
}
