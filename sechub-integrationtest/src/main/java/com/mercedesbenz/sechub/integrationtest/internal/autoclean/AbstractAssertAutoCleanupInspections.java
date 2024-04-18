// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal.autoclean;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.integrationtest.api.AbstractTestExecutable;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;

public abstract class AbstractAssertAutoCleanupInspections {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAssertAutoCleanupInspections.class);

    private static final String LINER = "************************************************************************";

    private List<AssertAutoCleanupAction> actions;

    protected AbstractAssertAutoCleanupInspections() {
        actions = new ArrayList<>();
    }

    /**
     * Adds an expectation about a delete count. Will not directly check but only
     * add the expectation. When all expectations have been added
     * {@link #assertAsExpected()} must be called to execute assertion /
     * verification.
     *
     * @param variant
     * @param className
     * @param amountOfDeletes
     * @return inspection object
     */
    public AbstractAssertAutoCleanupInspections addExpectedDeleteInspection(String variant, String className, int amountOfDeletes) {
        actions.add(new AssertAutoCleanupDeleteCountFoundAction(variant, className, amountOfDeletes));
        return this;
    }

    public AbstractAssertAutoCleanupInspections addExpectedNeverAnyDeleteInspection() {
        actions.add(new AssertAutoCleanupNeverAnyDeleteCountFoundAction());
        return this;
    }

    /**
     * Add an expectation about the amount of different kind of inspections
     * (inspections are uniquely identified by variant + class name)
     *
     * @param expectedAmount
     * @return
     */
    public AbstractAssertAutoCleanupInspections addExpectedDifferentKindOfDeleteInspections(int expectedAmount) {
        actions.add(new AssertAutoCleanupDeleteCountAmountAction(expectedAmount));
        return this;
    }

    @FunctionalInterface
    protected interface TestAutoCleanJsonDeleteCountFetcher {
        List<TestAutoCleanJsonDeleteCount> fetchCounts();
    }

    /**
     * Will start check for the expected parts. If failing a retry will be done -
     * until time out. When timeout has reached an exception will be thrown and test
     * fails. Attention: in case of {@link ActionState#PLEASE_GO_FURTHER} (e.g. for
     * {@link #addExpectedNeverAnyDeleteInspection()} ) always retries will be done,
     * which means: a timeout is here also the amount of time because always a retry
     * is done!
     *
     * Time to wait for next retry is 300 milliseconds.
     *
     * @param timeoutInSeconds time out in seconds
     */
    public void assertAsExpectedWithTimeOut(int timeoutInSeconds) {
        assertAsExpectedWithTimeOut(timeoutInSeconds, 300);
    }

    /**
     * Will start check for the expected parts. If failing a retry will be done -
     * until time out. When timeout has reached an exception will be thrown and test
     * fails. Attention: in case of {@link ActionState#PLEASE_GO_FURTHER} (e.g. for
     * {@link #addExpectedNeverAnyDeleteInspection()} ) always retries will be done,
     * which means: a timeout is here also the amount of time because always a retry
     * is done!
     *
     * @param timeoutInSeconds         time out in seconds
     * @param timeToWaitInMilliseconds time in milliseconds to wait before next
     *                                 retry
     */
    public void assertAsExpectedWithTimeOut(int timeoutInSeconds, int timeToWaitInMilliseconds) {
        TestAutoCleanJsonDeleteCountFetcher fetcher = createFeatcher();
        assertAsExpectedWithTimeOut(timeoutInSeconds, fetcher, timeToWaitInMilliseconds);
    }

    protected abstract TestAutoCleanJsonDeleteCountFetcher createFeatcher();

    private void assertAsExpectedWithTimeOut(int timeoutInSeconds, TestAutoCleanJsonDeleteCountFetcher fetcher, int timeToWaitInMilliseconds) {
        List<String> problems = new ArrayList<>();
        try {
            executeUntilSuccessOrTimeout(new AbstractTestExecutable(TestAPI.SUPER_ADMIN, timeoutInSeconds, timeToWaitInMilliseconds) {

                @Override
                public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                    List<TestAutoCleanJsonDeleteCount> counts = fetcher.fetchCounts();
                    StringBuilder problemMessageBuilder = new StringBuilder();

                    boolean atLeastOneWantsToRetry = false;
                    for (AssertAutoCleanupAction action : actions) {
                        if (action instanceof AssertAutoCleanupDeleteCountAction) {
                            AssertAutoCleanupDeleteCountAction deleteAction = (AssertAutoCleanupDeleteCountAction) action;
                            ActionState state = deleteAction.validate(counts, problemMessageBuilder);
                            /*
                             * Special case when ActionState.PLEASE_GO_FURTHER - always retry
                             */
                            atLeastOneWantsToRetry = atLeastOneWantsToRetry || state == ActionState.PLEASE_GO_FURTHER;
                        }
                    }
                    boolean noProblems = problemMessageBuilder.length() == 0;
                    if (noProblems) {
                        if (atLeastOneWantsToRetry) {
                            LOG.debug("No problems found, but at least one retry necessary, return false");
                            return false;
                        }
                        LOG.debug("No problems found, no retry necessary, return true");
                        return true;
                    }
                    problemMessageBuilder.append("\n\n").append(LINER).append("\nReturned data from integration test server:\n").append(LINER);
                    for (TestAutoCleanJsonDeleteCount count : counts) {
                        problemMessageBuilder.append("\n - count: variant=").append(count.variant).append(", deleted=").append(count.deleteCount)
                                .append(",className=").append(count.className);
                    }
                    problemMessageBuilder.append("\n");
                    problems.add("\nProblem(s):\n" + LINER + "\n" + problemMessageBuilder.toString());

                    LOG.debug("Problems found, return false");
                    return false;
                }
            });
        } catch (AssertionError e) {
            if (problems.size() == 0) {
                /*
                 * not really a failure - action just always run through - see
                 * ActionState#PLEASE_GO_FURTHER
                 */
                return;
            }
            Assert.fail("Failed with:" + e.getMessage() + "\n" + problems.toString());
        }

    }

}
