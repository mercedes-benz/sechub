package com.mercedesbenz.sechub.integrationtest.internal.autoclean;

import java.util.List;
import java.util.Objects;

public class AssertAutoCleanupDeleteCountFoundAction implements AssertAutoCleanupDeleteCountAction {

    private String variant;
    private String className;
    private int expectedDeletes;

    public AssertAutoCleanupDeleteCountFoundAction(String variant, String className, int expectedDeletes) {
        this.variant = variant;
        this.className = className;
        this.expectedDeletes = expectedDeletes;
    }

    public ActionState validate(List<TestAutoCleanJsonDeleteCount> counts, StringBuilder problemMessageBuilder) {
        boolean found = false;
        for (TestAutoCleanJsonDeleteCount count : counts) {
            if (!Objects.equals(variant, count.variant)) {
                continue;
            }
            if (!Objects.equals(className, count.className)) {
                continue;
            }
            if (expectedDeletes != count.deleteCount) {
                continue;
            }
            found = true;
            break;
        }
        if (!found) {
            problemMessageBuilder.append("\nAuto clean entry not found for ");
            problemMessageBuilder.append("variant=").append(variant).append(", expectedDeletes=").append(expectedDeletes).append(",className=")
                    .append(className);
        }
        return ActionState.DONE_CAN_MAKE_STATEMENT;
    }
}