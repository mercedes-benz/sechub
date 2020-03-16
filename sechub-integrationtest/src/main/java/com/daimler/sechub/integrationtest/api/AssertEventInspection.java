package com.daimler.sechub.integrationtest.api;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.assertj.core.util.Arrays;

import com.daimler.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.daimler.sechub.integrationtest.internal.TestJSONHelper;
import com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventHistory;
import com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventHistoryInspection;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.fasterxml.jackson.core.JsonProcessingException;

public class AssertEventInspection {

    private static final int DEFAULT_TIMEOUT_IN_SECONDS = 3;

    private IntegrationTestEventHistory history;

    private AssertEventInspection() {
    }

    /**
     * Waits for last inspection having given id and expected receiver classes.
     * Ensures that the last expected inspection has been finally done by checking
     * the last receivers. <br>
     * <br>
     * Uses {@link #DEFAULT_TIMEOUT_IN_SECONDS} to time out
     * 
     * @param lastInspectionId
     * @param lastReceiverClassNames
     * @return inspection
     */
    public static AssertEventInspection assertLastInspection(int lastInspectionId, MessageID messageId, String... lastReceiverClassNames) {
        return assertLastInspection(DEFAULT_TIMEOUT_IN_SECONDS, lastInspectionId, messageId, lastReceiverClassNames);
    }

    /**
     * Waits for last inspection having given id and expected receiver classes.
     * Ensures that the last expected inspection has been finally done
     * 
     * @param lastInspectionId
     * @param lastReceiverClassNames
     * @param timeOutInSeconds
     * @param messageId
     * @return inspection
     */
    public static AssertEventInspection assertLastInspection(int timeOutInSeconds, int lastInspectionId, MessageID messageId,
            String... lastReceiverClassNames) {
        if (!EventInspectionAPI.fetchIsStarted()) {
            fail("Event inspection is not started. Maybe you forget to invoke TestAPI.startEventInspection() before ?");
        }
        AssertEventInspection inspection = new AssertEventInspection();
        inspection.waitForEventsSent(timeOutInSeconds, lastInspectionId, messageId, lastReceiverClassNames);
        return inspection;
    }

    private void waitForEventsSent(int maxTimeInSeconds, int lastInspectionId, MessageID messageId, String... expectedLastReceiverClassNames) {
        // Info about lastInspectionId: it starts with 0, so first entry = 0, second =
        // 1,...
        int inspectionSize = lastInspectionId + 1;

        /* prepare */
        int waitInMillis = 1000;
        history = null;
        int second = 0;
        do {
            second++;
            if (second > maxTimeInSeconds) {
                StringBuilder sb = new StringBuilder();
                sb.append("Wait for events sent timed out:").append(waitInMillis * second).append(" ms\n\n");
                sb.append("Expected:\n -lastInspectionId:").append(lastInspectionId).append("\n");
                sb.append(" - message id:").append(messageId.getId()).append("\n");
                sb.append(" - receiverClassNames:");
                for (String className : expectedLastReceiverClassNames) {
                    sb.append("\n    class: ").append(className);
                }
                sb.append("Latest fetched event history was:\n");
                if (history == null) {
                    sb.append("null");
                } else {
                    String historyJSONprettyPrinted = null;
                    try {
                        historyJSONprettyPrinted = TestJSONHelper.get().getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(history);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("json pretty print failed", e);
                    }
                    sb.append("\nFetched history:\n");
                    sb.append(historyJSONprettyPrinted);
                }
                sb.append("\n");
                throw new IllegalStateException(sb.toString());
            }
            history = TestAPI.fetchEventInspectionHistory();

            waitMilliSeconds(waitInMillis);

        } while (history.getIdToInspectionMap().size() < inspectionSize
                || !areReceiversAsExpected(lastInspectionId, messageId, expectedLastReceiverClassNames));

    }

    public AssertEventInspection assertSender(int inspectionId, MessageID messageId, String expectedSenderClassName) {
        IntegrationTestEventHistoryInspection inspection = history.getIdToInspectionMap().get(inspectionId);
        if (inspection == null) {
            fail("No event inspection with inspection-id:" + inspectionId + " found!");
        }
        if (!messageId.getId().equals(inspection.getEventId())) {
                fail("Inspection-id:" + inspectionId + " found, but message id differs! Expected message id:" + messageId.getId() + " , but was:"
                        + inspection.getEventId());
        }
        assertEquals("Sender class name not as expected!", expectedSenderClassName, inspection.getSenderClassName());
        return this;
    }

    public AssertEventInspection assertReceivers(int inspectionId, MessageID messageId, String... expectedReiverClassNames) {
        commonReceiversAsExpected(inspectionId, true, messageId, expectedReiverClassNames);
        return this;
    }

    public boolean areReceiversAsExpected(int inspectionId, MessageID messageId, String... expectedReiverClassNames) {
        return commonReceiversAsExpected(inspectionId, false, messageId, expectedReiverClassNames);
    }

    private boolean commonReceiversAsExpected(int inspectionId, boolean fail, MessageID messageId, String... expectedReiverClassNames) {
        IntegrationTestEventHistoryInspection inspection = history.getIdToInspectionMap().get(inspectionId);
        if (inspection == null) {
            if (fail) {
                fail("No event inspection with inspection-id:" + inspectionId + " found!");
            }
            return false;
        }
        if (!messageId.getId().equals(inspection.getEventId())) {
            if (fail) {
                fail("Inspection-id:" + inspectionId + " found, but message id differs! Expected message id:" + messageId.getId() + " , but was:"
                        + inspection.getEventId());
            }
            return false;
        }
        List<Object> expectedReceiverClassNamesAsList = Arrays.asList(expectedReiverClassNames);
        List<String> foundReceiverClassNames = inspection.getReceiverClassNames();
        if (!foundReceiverClassNames.containsAll(expectedReceiverClassNamesAsList)) {
            failBecauseOfReceiverClassNameDiff(inspectionId, messageId, expectedReceiverClassNamesAsList, foundReceiverClassNames);
            return false;
        }
        if (expectedReceiverClassNamesAsList.size() != foundReceiverClassNames.size()) {
            failBecauseOfReceiverClassNameDiff(inspectionId, messageId, expectedReceiverClassNamesAsList, foundReceiverClassNames);
            return false;
        }
        return true;
    }

    private void failBecauseOfReceiverClassNameDiff(int inspectionId, MessageID messageId, List<Object> expectedReceiverClassNamesAsList, List<String> receiverClassNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("Receivers not as expected for inspection id:" + inspectionId + ", messageId:"+messageId);
        sb.append("\nExpected:\n");
        for (Object className : expectedReceiverClassNamesAsList) {
            sb.append("- ");
            sb.append(className);
            sb.append("\n");
        }
        sb.append("\nResult:\n");

        for (Object className : receiverClassNames) {
            sb.append("- ");
            sb.append(className);
            sb.append("\n");
        }
        fail(sb.toString());
    }

    /**
     * Writes history to
     * "sechub-integrationtest/build/test-results/event-trace/${id.toLowerCase()}.json"
     * 
     * @param id
     */
    public void writeHistoryToFile(String id) {
        /* write to build folder, so we can use it in documentation generation */
        IntegrationTestFileSupport testfileSupport = IntegrationTestFileSupport.getTestfileSupport();
        File file = new File(testfileSupport.getRootFolder(), "sechub-integrationtest/build/test-results/event-trace/" + id.toLowerCase() + ".json");
        testfileSupport.writeTextFile(file, history.toJSON());

    }
}
