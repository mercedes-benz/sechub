package com.mercedesbenz.sechub.integrationtest.internal.autoclean;

import java.util.List;

public interface AssertAutoCleanupDeleteCountAction extends AssertAutoCleanupAction {
    public ActionState validate(List<TestAutoCleanJsonDeleteCount> counts, StringBuilder problemMessageBuilder);
}