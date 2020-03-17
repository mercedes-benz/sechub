package com.daimler.sechub.integrationtest.api;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
                sb.append("\nLatest fetched event history was:\n");
                if (history == null) {
                    sb.append("null");
                } else {
                    String historyJSONprettyPrinted = null;
                    try {
                        historyJSONprettyPrinted = TestJSONHelper.get().getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(history);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("json pretty print failed", e);
                    }
                    sb.append(historyJSONprettyPrinted);
                }
                sb.append("\n");
                sb.append(createAssertTestExampleCode());
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
                        + inspection.getEventId()+createAssertTestExampleCode());
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
                fail("No event inspection with inspection-id:" + inspectionId + " found!"+createAssertTestExampleCode());
            }
            return false;
        }
        if (!messageId.getId().equals(inspection.getEventId())) {
            if (fail) {
                fail("Inspection-id:" + inspectionId + " found, but message id differs! Expected message id:" + messageId.getId() + " , but was:"
                        + inspection.getEventId()+createAssertTestExampleCode());
            }
            return false;
        }
        List<String> expectedReceiverClassNamesAsList = Arrays.asList(expectedReiverClassNames);
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
    
    private String createAssertTestExampleCode() {
        /* @formatter:off Example: */
        
//        AssertEventInspection.
//        assertLastInspection(1,
//            MessageID.SCHEDULER_JOB_PROCESSING_DISABLED,
//            "com.daimler.sechub.domain.administration.scheduler.SchedulerAdministrationMessageHandler",
//            "com.daimler.sechub.domain.notification.NotificationMessageHandler").
//        assertSender(1, 
//            MessageID.SCHEDULER_JOB_PROCESSING_DISABLED, 
//            "com.daimler.sechub.domain.schedule.config.SchedulerConfigService").
//        /* sanity check:*/
//        assertReceivers(0, 
//            MessageID.REQUEST_SCHEDULER_DISABLE_JOB_PROCESSING, 
//            "com.daimler.sechub.domain.schedule.ScheduleMessageHandler").
//        /* sanity check:*/
//        assertSender(0, 
//            MessageID.REQUEST_SCHEDULER_DISABLE_JOB_PROCESSING, 
//            "com.daimler.sechub.domain.administration.scheduler.SwitchSchedulerJobProcessingService").
//        /* write */
//        writeHistoryToFile(UseCaseIdentifier.UC_ADMIN_DISABLES_SCHEDULER_JOB_PROCESSING.name());
        /* @formatter:on */
    
    
        StringBuilder sb = new StringBuilder();
        sb.append("\nBut if this is not a failure but correct and you are just writing your test/fixing changinges, you can just paste following test code:\n\n");
        if (history==null) {
            sb.append("Impossible - history == null");
            return sb.toString();
        }
        int lasltIdentifierId = history.getIdToInspectionMap().size()-1;
        sb.append(" AssertEventInspection.\n");
        /* last inspection */
        sb.append("     assertLastInspection("+lasltIdentifierId+",\n");
        IntegrationTestEventHistoryInspection lastInspection = history.getIdToInspectionMap().get(lasltIdentifierId);
        if (lastInspection==null) {
            sb.append("Impossible - history = null");
            return sb.toString();
        }
        sb.append("           MessageID."+lastInspection.getEventId()+",\n");
        for (Iterator<String> it =lastInspection.getReceiverClassNames().iterator() ; it.hasNext(); ) {
            String receiverClassName = it.next();
            sb.append("           \""+receiverClassName+"\"");
            if (it.hasNext()) {
                sb.append(",");
            }else {
                sb.append(").");
            }
            sb.append("\n");
        }
        /* other inspections */
        for (Iterator<Integer> it =  history.getIdToInspectionMap().keySet().iterator();it.hasNext();) {
            Integer inspectionId = it.next();
            if (inspectionId.intValue()!=lasltIdentifierId) {
                IntegrationTestEventHistoryInspection inspection = history.getIdToInspectionMap().get(inspectionId);
                sb.append("     assertSender("+inspectionId+",\n");
                sb.append("           MessageID."+inspection.getEventId()+",\n");
                sb.append("           \""+inspection.getSenderClassName()+"\").\n");
                sb.append("     assertReceivers("+inspectionId+",\n");
                sb.append("           MessageID."+inspection.getEventId()+",\n");
                for (Iterator<String> it2 =inspection.getReceiverClassNames().iterator() ; it2.hasNext(); ) {
                    String receiverClassName = it2.next();
                    sb.append("           \""+receiverClassName+"\"");
                    if (it2.hasNext()) {
                        sb.append(",");
                    }else {
                        sb.append(").");
                    }
                    sb.append("\n");
                }
            }
        }
        sb.append("     /* write */\n");
        sb.append("     writeHistoryToFile(UseCaseIdentifier.UC_CHANGEME.name());\n");
        return sb.toString();
    }

    private void failBecauseOfReceiverClassNameDiff(int inspectionId, MessageID messageId, List<String> expectedReceiverClassNamesAsList, List<String> receiverClassNames) {
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
        sb.append(createAssertTestExampleCode());
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
