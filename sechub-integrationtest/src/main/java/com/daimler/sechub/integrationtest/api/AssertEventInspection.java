package com.daimler.sechub.integrationtest.api;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import com.daimler.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.daimler.sechub.integrationtest.internal.TestJSONHelper;
import com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventHistory;
import com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventHistoryInspection;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.fasterxml.jackson.core.JsonProcessingException;

public class AssertEventInspection {

    private static final int DEFAULT_TIMEOUT_IN_SECONDS = 3;

    private IntegrationTestEventHistory history;
    private int timeoutInSeconds;

    public static AssertEventInspection assertEventInspection() {
        return assertEventInspection(DEFAULT_TIMEOUT_IN_SECONDS);
    }

    public static AssertEventInspection assertEventInspection(int timeOutInSeconds) {
        if (!EventInspectionAPI.fetchIsStarted()) {
            fail("Event inspection is not started. Maybe you forget to invoke TestAPI.startEventInspection() before ?");
        }
        return new AssertEventInspection(timeOutInSeconds);
    }

    /** @formatter:off
    *
    * Will always fail but generates a test case proposal for current event situation. 
    * Start the test, copy proposal snippet (at the end of first stacktrace element) and
    * it will work. This is very convenient to create new event trace tests... 
    *  
    * (of course you should still validate that the generated test is correct)
    * @formatter:on */
    public static void assertEventInspectionFailsButGeneratesTestCaseProposal() {
        assertEventInspectionFailsButGeneratesTestCaseProposal(5);

    }

    /** @formatter:off
     *
     * Will always fail but generates a test case proposal for current event situation. 
     * Start the test, copy proposal snippet (at the end of first stacktrace element) and
     * it will work. This is very convenient to create new event trace tests... 
     *  
     * (of course you should still validate that the generated test is correct)
     * @param waitSeconds amount of seconds to wait before fetching history and generating test case
     * @formatter:on */
    public static void assertEventInspectionFailsButGeneratesTestCaseProposal(int waitSeconds) {
        /* we weait for events handled */
        waitSeconds(waitSeconds);
        IntegrationTestEventHistory history2 = TestAPI.fetchEventInspectionHistory();
        fail("Unimplemented testcase. Please use next generated test snippet inside your test:\n\n" + createAssertTestExampleCodeSnippet(history2));
    }

    private AssertEventInspection(int timeOutInSeconds) {
        this.timeoutInSeconds = timeOutInSeconds;
    }

    public AssertEventInspectionExpectation expect() {
        return new AssertEventInspectionExpectation();
    }

    public class AssertEventInspectionExpectation {

        private List<AssertEventInspectionExpectionEntry> entries = new ArrayList<>();

        private AssertEventInspectionExpectation() {

        }

        public AssertEventInspectionExpectionEntry asyncEvent(MessageID messageId) {
            AssertEventInspectionExpectionEntry entry = new AssertEventInspectionExpectionEntry(messageId, false);
            entries.add(entry);
            return entry;
        }

        public AssertEventInspectionExpectionEntry syncEvent(MessageID messageId) {
            AssertEventInspectionExpectionEntry entry = new AssertEventInspectionExpectionEntry(messageId, true);
            entries.add(entry);
            return entry;
        }

        public class AssertEventInspectionExpectionEntry {

            private MessageID messageId;
            private String senderClassname;
            private List<String> receiverClassnames = new ArrayList<>();
            private boolean synchronEvent;

            private AssertEventInspectionExpectionEntry(MessageID messageId, boolean synchronEvent) {
                this.messageId = messageId;
                this.synchronEvent = synchronEvent;
            }

            public AssertEventInspectionExpectionEntry asyncEvent(MessageID messageId) {
                return back().asyncEvent(messageId);
            }

            public AssertEventInspectionExpectionEntry from(String senderClassname) {
                this.senderClassname = senderClassname;
                return this;
            }

            public AssertEventInspectionExpectionEntry to(String... receiverClassnames) {
                if (receiverClassnames == null) {
                    return this;
                }
                this.receiverClassnames.addAll(Arrays.asList(receiverClassnames));
                return this;
            }

            private AssertEventInspectionExpectation back() {
                return AssertEventInspectionExpectation.this;
            }

            public void assertAsExpectedAndCreateHistoryFile(String id) {
                back().assertAndGenerateHistoryFile(id);
            }

        }

        private void assertAndGenerateHistoryFile(String id) {

            waitForExpectedAmountOfSendersAndReceivers(timeoutInSeconds);

            assertHistoryIsAsExpected();

            writeHistoryToFile(id);

        }

        /**
         * Writes history to
         * "sechub-integrationtest/build/test-results/event-trace/${id.toLowerCase()}.json"
         * 
         * @param id
         */
        private void writeHistoryToFile(String id) {
            /* write to build folder, so we can use it in documentation generation */
            IntegrationTestFileSupport testfileSupport = IntegrationTestFileSupport.getTestfileSupport();
            File file = new File(testfileSupport.getRootFolder(), "sechub-integrationtest/build/test-results/event-trace/" + id.toLowerCase() + ".json");
            testfileSupport.writeTextFile(file, history.toJSON());

        }

        /*
         * This method will only inspect that each pair of eventFromTo data is
         * contained! No matter which ordering (because we cannot ensure/proof this for
         * async events...
         */
        private void assertHistoryIsAsExpected() {
            if (history == null) {
                throw new EventInspectionStateException("History may not be null!", "No details");
            }

            SortedMap<Integer, IntegrationTestEventHistoryInspection> map = history.getIdToInspectionMap();

            /* foreach - find messageid+ from in both and check containing same receivers */
            List<IntegrationTestEventHistoryInspection> missing = new ArrayList<>(map.values());
            List<IntegrationTestEventHistoryInspection> inspectionsExpected = new ArrayList<>();

            /* build inspectionsExpected list */
            for (AssertEventInspectionExpectionEntry entry : entries) {
                IntegrationTestEventHistoryInspection insp = new IntegrationTestEventHistoryInspection();
                if (entry.synchronEvent) {
                    insp.setSynchronousSender(entry.senderClassname, entry.messageId);
                } else {
                    insp.setAsynchronousSender(entry.senderClassname, entry.messageId);
                }
                for (String receiver : entry.receiverClassnames) {
                    insp.getReceiverClassNames().add(receiver);
                }
                inspectionsExpected.add(insp);
            }

            /* check if all expected parts contained */
            for (IntegrationTestEventHistoryInspection inspection : map.values()) {
                if (inspectionsExpected.contains(inspection)) {
                    missing.remove(inspection);
                } else {
                    StringBuilder details = new StringBuilder();
                    details.append("Found in history, but not expected:").append(inspection);
                    throw new EventInspectionStateException("Unexpected entry found", details.toString());
                }
            }
            if (!missing.isEmpty()) {
                StringBuilder details = new StringBuilder();
                details.append("Expected, but missing in history:").append(missing);
                throw new EventInspectionStateException("Unexpected entry found", details.toString());
            }
            /* check missing */
        }

        /* wait until expected amount of senders and receivers is matched */
        private void waitForExpectedAmountOfSendersAndReceivers(int maxTimeInSeconds) {
            AssertEventContext context = new AssertEventContext();
            context.expectedAmountOfSenders = entries.size();
            context.expectedAmountOfReceivers = 0;
            for (AssertEventInspectionExpectionEntry entry : entries) {
                context.expectedAmountOfReceivers += entry.receiverClassnames.size();
            }

            int waitInMillis = 1000;
            history = null;
            context.seconds = 0;
            do {
                if (context.seconds > maxTimeInSeconds) {
                    failOnWait(context);
                }
                waitMilliSeconds(waitInMillis);

                history = TestAPI.fetchEventInspectionHistory();

                SortedMap<Integer, IntegrationTestEventHistoryInspection> map = history.getIdToInspectionMap();
                context.foundSenders = map.size();
                context.foundReceivers = 0;
                for (Iterator<IntegrationTestEventHistoryInspection> it = map.values().iterator(); it.hasNext();) {
                    IntegrationTestEventHistoryInspection inspection = it.next();
                    context.foundReceivers += inspection.getReceiverClassNames().size();
                }
                context.seconds++;

            } while (context.foundSenders < context.expectedAmountOfSenders || context.foundReceivers < context.expectedAmountOfReceivers);

            if (context.foundSenders != context.expectedAmountOfSenders || context.foundReceivers != context.expectedAmountOfReceivers) {
                /* something odd! */
                failOnWait(context);
            }

        }
    }

    private class AssertEventContext {
        int expectedAmountOfSenders;
        int expectedAmountOfReceivers;
        int foundSenders;
        int foundReceivers;
        int seconds;
    }

    private void failOnWait(AssertEventContext context) {
        String message;
        StringBuilder details = new StringBuilder();

        message = "Wait for events sent timed out:" + (context.seconds) + " seconds";

        details.append("\nExpected:");
        details.append("\n - senders:").append(context.expectedAmountOfSenders);
        details.append("\n - receivers:").append(context.expectedAmountOfReceivers);
        details.append("\nFound:");
        details.append("\n - senders:").append(context.foundSenders);
        details.append("\n - receivers:").append(context.foundReceivers);
        details.append("\nLatest fetched event history was:\n");
        if (history == null) {
            details.append("null");
        } else {
            String historyJSONprettyPrinted = null;
            try {
                historyJSONprettyPrinted = TestJSONHelper.get().getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(history);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("json pretty print failed", e);
            }
            details.append(historyJSONprettyPrinted);
        }
        details.append("\n");
        throw new EventInspectionStateException(message, details.toString());
    }

    private class EventInspectionStateException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EventInspectionStateException(String message, String details) {
            super(message + "\n(Please look at the end for proposal snippet!)\n" + details + createAssertTestExampleCodeSnippet(history));
        }
    }

    private static String createAssertTestExampleCodeSnippet(IntegrationTestEventHistory history) {
        /* @formatter:off Example: */
        
//     AssertEventInspection.
//        assertEventInspection().
//        expect().
//            asyncEvent(MessageID.USER_ADDED_TO_PROJECT).
//                from("com.daimler.sechub.domain.administration.project.ProjectAssignUserService").
//                to("com.daimler.sechub.domain.schedule.ScheduleMessageHandler", 
//                   "com.daimler.sechub.domain.scan.ScanMessageHandler").
//            syncEvent(MessageID.USER_ADDED_TO_PROJECT2).
//                from("com.daimler.sechub.domain.administration.project.ProjectAssignUserService2").
//                to("com.daimler.sechub.domain.schedule.ScheduleMessageHandler2", 
//                   "com.daimler.sechub.domain.scan.ScanMessageHandler2").
//        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_ASSIGNS_USER_TO_PROJECT.name());
        /* @formatter:on */

        StringBuilder sb = new StringBuilder();
        sb.append(
                "\nBut if this is not a failure but correct and you are just writing your test/fixing changinges, you can just paste following test code:\n\n");
        if (history == null) {
            sb.append("Impossible - history == null");
            return sb.toString();
        }
        sb.append(" AssertEventInspection.assertEventInspection().\n");
        sb.append("  expect().\n");

        for (Iterator<Integer> inspectionIt = history.getIdToInspectionMap().keySet().iterator(); inspectionIt.hasNext();) {
            Integer inspectionId = inspectionIt.next();
            IntegrationTestEventHistoryInspection inspection = history.getIdToInspectionMap().get(inspectionId);
            sb.append("     /* " + inspectionId + " */\n");
            sb.append("     ");
            if (inspection.isSynchron()) {
                sb.append("sync");
            } else {
                sb.append("async");
            }
            sb.append("Event(MessageID." + inspection.getEventId() + ").\n");
            sb.append("           from(\"" + inspection.getSenderClassName() + "\").\n");
            sb.append("           to(");
            Iterator<String> receiverIt = inspection.getReceiverClassNames().iterator();

            int receivers = 0;

            if (receiverIt.hasNext()) {
                /* with content */
                while (receiverIt.hasNext()) {
                    String receiverClassName = receiverIt.next();
                    if (receivers != 0) {
                        sb.append("              ");
                    }
                    sb.append("\"" + receiverClassName + "\"");
                    if (receiverIt.hasNext()) {
                        sb.append(",");
                    } else {
                        sb.append(").");
                    }
                    sb.append("\n");
                    receivers++;
                }
            } else {
                /* no content */
                sb.append(").\n");
            }
        }
        sb.append("  /* assert + write */\n");
        sb.append("  assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_CHANGEME.name());\n");
        return sb.toString();
    }

}
