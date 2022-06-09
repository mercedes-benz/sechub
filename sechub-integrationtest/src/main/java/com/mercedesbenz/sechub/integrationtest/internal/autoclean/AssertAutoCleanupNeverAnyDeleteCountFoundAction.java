package com.mercedesbenz.sechub.integrationtest.internal.autoclean;

import java.util.List;

public class AssertAutoCleanupNeverAnyDeleteCountFoundAction implements AssertAutoCleanupDeleteCountAction {

    public ActionState validate(List<TestAutoCleanJsonDeleteCount> counts, StringBuilder problemMessageBuilder) {
        if (counts.size() == 0) {
            return ActionState.PLEASE_GO_FURTHER;
        }
        problemMessageBuilder
                .append("\nAmount of auto clean delete count entries not as expected. Found: " + counts.size() + ", but expected never any entry!");
        return ActionState.DONE_CAN_MAKE_STATEMENT;
    }
}
