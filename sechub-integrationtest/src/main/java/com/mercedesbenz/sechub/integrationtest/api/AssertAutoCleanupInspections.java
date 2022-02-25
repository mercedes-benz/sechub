package com.mercedesbenz.sechub.integrationtest.api;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import wiremock.com.google.common.base.Objects;

import com.mercedesbenz.sechub.integrationtest.internal.TestJsonDeleteCount;

public class AssertAutoCleanupInspections {

    protected static final String LINER = "************************************************************************";

    public static AssertAutoCleanupInspections assertAutoCleanupInspections() {
        return new AssertAutoCleanupInspections();
    }

    private List<AssertAutoCleanupAction> actions;

    private AssertAutoCleanupInspections() {
        actions = new ArrayList<>();
    }

    /**
     * Adds an expectation about a delete count. Will not directly check but only
     * add the expectation. When all expectations has been added
     * {@link #assertAsExpected()} must be called to execute assertion /
     * verification.
     *
     * @param variant
     * @param className
     * @param amountOfDeletes
     * @return inspection object
     */
    public AssertAutoCleanupInspections addExpectedDeleteInspection(String variant, String className, int amountOfDeletes) {
        actions.add(new AssertAutoCleanupDeleteCountFoundAction(variant, className, amountOfDeletes));
        return this;
    }

    public AssertAutoCleanupInspections addExpectedNeverAnyDeleteInspection() {
        actions.add(new AssertAutoCleanupNeverAnyDeleteCountFoundAction());
        return this;
    }

    /**
     * Add an expectation about the amount of different kind of inspections
     * (inspections are unique identified by variant + class name)
     *
     * @param expectedAmount
     * @return
     */
    public AssertAutoCleanupInspections addExpectedDifferentKindOfDeleteInspections(int expectedAmount) {
        actions.add(new AssertAutoCleanupDeleteCountAmountAction(expectedAmount));
        return this;
    }

    /**
     * Will start check for the expected parts. If failing a retry will be done -
     * until time out. When timeout has reached an exception will be thrown and test
     * fails.
     */
    @SuppressWarnings("unchecked")
    public void assertAsExpectedWithTimeOut(int timeoutInSeconds) {
        List<String> problems = new ArrayList<>();
        try {
            TestAPI.executeUntilSuccessOrTimeout(new AbstractTestExecutable(TestAPI.SUPER_ADMIN, timeoutInSeconds, 300) {

                @Override
                public boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception {
                    List<TestJsonDeleteCount> counts = TestAPI.fetchAutoCleanupInspectionDeleteCounts();
                    StringBuilder problemMessageBuilder = new StringBuilder();

                    boolean atLeastOneWantsToRetry = false;
                    for (AssertAutoCleanupAction action : actions) {
                        if (action instanceof AssertAutoCleanupDeleteCountAction) {
                            AssertAutoCleanupDeleteCountAction deleteAction = (AssertAutoCleanupDeleteCountAction) action;
                            ActionState state = deleteAction.validate(counts, problemMessageBuilder);
                            atLeastOneWantsToRetry = atLeastOneWantsToRetry || state == ActionState.PLEASE_GO_FURTHER;
                        }
                    }
                    boolean noProblems = problemMessageBuilder.length() == 0;
                    if (noProblems) {
                        if (atLeastOneWantsToRetry) {
                            return false;
                        }
                        return true;
                    }
                    problemMessageBuilder.append("\n\n").append(LINER).append("\nReturned data from integration test server:\n").append(LINER);
                    for (TestJsonDeleteCount count : counts) {
                        problemMessageBuilder.append("\n - variant=").append(count.variant).append(", expectedDeletes=").append(count.deleteCount)
                                .append(",className=").append(count.className);
                    }
                    problemMessageBuilder.append("\n");
                    problems.add("\nProblem(s):\n" + LINER + "\n" + problemMessageBuilder.toString());
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

    private enum ActionState {
        DONE_CAN_MAKE_STATEMENT, PLEASE_GO_FURTHER,
    }

    private interface AssertAutoCleanupAction {

    }

    private interface AssertAutoCleanupDeleteCountAction extends AssertAutoCleanupAction {
        public ActionState validate(List<TestJsonDeleteCount> counts, StringBuilder problemMessageBuilder);
    }

    private class AssertAutoCleanupDeleteCountFoundAction implements AssertAutoCleanupDeleteCountAction {

        private String variant;
        private String className;
        private int expectedDeletes;

        public AssertAutoCleanupDeleteCountFoundAction(String variant, String className, int expecctedDeletes) {
            this.variant = variant;
            this.className = className;
            this.expectedDeletes = expecctedDeletes;
        }

        public ActionState validate(List<TestJsonDeleteCount> counts, StringBuilder problemMessageBuilder) {
            boolean found = false;
            for (TestJsonDeleteCount count : counts) {
                if (!Objects.equal(variant, count.variant)) {
                    continue;
                }
                if (!Objects.equal(className, count.className)) {
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

    private class AssertAutoCleanupDeleteCountAmountAction implements AssertAutoCleanupDeleteCountAction {

        private int expectedAmount;

        public AssertAutoCleanupDeleteCountAmountAction(int expectedAmount) {
            this.expectedAmount = expectedAmount;
        }

        public ActionState validate(List<TestJsonDeleteCount> counts, StringBuilder problemMessageBuilder) {
            if (expectedAmount == counts.size()) {
                return ActionState.DONE_CAN_MAKE_STATEMENT;
            }
            problemMessageBuilder
                    .append("\nAmount of auto clean delete count entries not as expected. Found: " + counts.size() + ", but expected:" + expectedAmount);
            return ActionState.DONE_CAN_MAKE_STATEMENT;
        }
    }

    private class AssertAutoCleanupNeverAnyDeleteCountFoundAction implements AssertAutoCleanupDeleteCountAction {

        public ActionState validate(List<TestJsonDeleteCount> counts, StringBuilder problemMessageBuilder) {
            if (counts.size() == 0) {
                return ActionState.PLEASE_GO_FURTHER;
            }
            problemMessageBuilder
                    .append("\nAmount of auto clean delete count entries not as expected. Found: " + counts.size() + ", but expected never any entry!");
            return ActionState.DONE_CAN_MAKE_STATEMENT;
        }
    }

}
