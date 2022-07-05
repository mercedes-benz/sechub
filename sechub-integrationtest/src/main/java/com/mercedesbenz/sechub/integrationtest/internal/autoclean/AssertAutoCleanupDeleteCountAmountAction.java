// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal.autoclean;

import java.util.List;

public class AssertAutoCleanupDeleteCountAmountAction implements AssertAutoCleanupDeleteCountAction {

    private int expectedAmount;

    public AssertAutoCleanupDeleteCountAmountAction(int expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public ActionState validate(List<TestAutoCleanJsonDeleteCount> counts, StringBuilder problemMessageBuilder) {
        if (expectedAmount == counts.size()) {
            return ActionState.DONE_CAN_MAKE_STATEMENT;
        }
        problemMessageBuilder
                .append("\nAmount of auto clean delete count entries not as expected. Found: " + counts.size() + ", but expected:" + expectedAmount);
        return ActionState.DONE_CAN_MAKE_STATEMENT;
    }
}