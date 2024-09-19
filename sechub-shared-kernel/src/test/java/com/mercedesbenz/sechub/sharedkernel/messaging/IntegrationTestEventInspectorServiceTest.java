// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.sharedkernel.test.SimulatedDomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.test.sechub.domain.testonly.SimulatedCaller;

import ch.qos.logback.classic.Level;

public class IntegrationTestEventInspectorServiceTest {

    private IntegrationTestEventInspectorService serviceToTest;
    private DomainMessage request;
    private AsynchronMessageHandler asynchronousMessagerHandler;
    private SynchronMessageHandler synchronousMessagerHandler;
    private SimulatedDomainMessageService simulatedDomainMessageService;
    private SimulatedCaller simulatedCaller;

    @Before
    public void before() {
        serviceToTest = new IntegrationTestEventInspectorService();

        simulatedDomainMessageService = new SimulatedDomainMessageService(serviceToTest);
        simulatedCaller = new SimulatedCaller(simulatedDomainMessageService);

        request = mock(DomainMessage.class);
        when(request.getMessageId()).thenReturn(MessageID.PROJECT_CREATED);
        asynchronousMessagerHandler = mock(AsynchronMessageHandler.class);
        synchronousMessagerHandler = mock(SynchronMessageHandler.class);

        if (IntegrationTestEventInspectorService.LOG instanceof ch.qos.logback.classic.Logger) {
            ((ch.qos.logback.classic.Logger) IntegrationTestEventInspectorService.LOG).setLevel(Level.TRACE);
        }

    }

    @After
    public void after() {
        if (IntegrationTestEventInspectorService.LOG instanceof ch.qos.logback.classic.Logger) {
            ((ch.qos.logback.classic.Logger) IntegrationTestEventInspectorService.LOG).setLevel(Level.DEBUG);
        }
    }

    @Test
    public void initial_returns_history_without_usecasename_and_no_entries() {

        /* test */
        IntegrationTestEventHistory history = serviceToTest.getHistory();
        assertNotNull(history);
        assertEquals(0, history.getIdToInspectionMap().keySet().size());
    }

    @Test
    public void reset_done_returns_history_without_usecasename_and_no_entries() {
        /* execute */
        serviceToTest.resetAndStop();
        serviceToTest.inspectSendAsynchron(request, 1);

        /* test */
        IntegrationTestEventHistory history = serviceToTest.getHistory();
        assertNotNull(history);
        assertEquals(0, history.getIdToInspectionMap().keySet().size());
        assertEquals(0, serviceToTest.inspectionIdCounter); // count not initialized until history enabled
    }

    @Test
    public void async_when_initialized_with_usecase_UC_aDMIN_CREATE_PROJECT_returns_history_without_usecasename() {
        /* execute */
        serviceToTest.start();
        simulatedCaller.simulateCallerSendAsync(123, request);
        serviceToTest.inspectReceiveAsynchronMessage(request, 123, asynchronousMessagerHandler);

        /* test */
        IntegrationTestEventHistory history = serviceToTest.getHistory();
        assertNotNull(history);
        Map<Integer, IntegrationTestEventHistoryInspection> idToInspectionMap = history.getIdToInspectionMap();
        assertEquals(1, idToInspectionMap.keySet().size());

        /* check inspection id as expected */
        Entry<Integer, IntegrationTestEventHistoryInspection> entry = idToInspectionMap.entrySet().iterator().next();
        assertEquals(Integer.valueOf(123), entry.getKey());

        IntegrationTestEventHistoryInspection inspection123 = entry.getValue();
        assertEquals(false, inspection123.isSynchron());

        /* check contains info about receiver as expected */
        List<String> receiverClassNames = inspection123.getReceiverClassNames();
        assertEquals(1, receiverClassNames.size());
        assertEquals(asynchronousMessagerHandler.getClass().getName(), receiverClassNames.iterator().next());

        /* check contains info about sender as expected */
        String expectedSenderClassName = simulatedCaller.getClass().getName();
        String senderClassName = inspection123.getSenderClassName();
        assertEquals(expectedSenderClassName, senderClassName);// test calls the inspection

    }

    @Test
    public void sync_when_started() {
        /* execute */
        serviceToTest.start();
        simulatedCaller.simulateCallereSendSync(123, request);
        serviceToTest.inspectReceiveSynchronMessage(request, 123, synchronousMessagerHandler);

        /* test */
        IntegrationTestEventHistory history = serviceToTest.getHistory();
        assertNotNull(history);
        Map<Integer, IntegrationTestEventHistoryInspection> idToInspectionMap = history.getIdToInspectionMap();
        assertEquals(1, idToInspectionMap.keySet().size());

        /* check inspection id as expected */
        Entry<Integer, IntegrationTestEventHistoryInspection> entry = idToInspectionMap.entrySet().iterator().next();
        assertEquals(Integer.valueOf(123), entry.getKey());

        IntegrationTestEventHistoryInspection inspection123 = entry.getValue();
        assertEquals(true, inspection123.isSynchron());

        /* check contains info about receiver as expected */
        List<String> receiverClassNames = inspection123.getReceiverClassNames();
        assertEquals(1, receiverClassNames.size());
        String expectedReceiverClassname = synchronousMessagerHandler.getClass().getName();
        String firstReceiverClassName = receiverClassNames.iterator().next();
        assertEquals(expectedReceiverClassname, firstReceiverClassName);

        /* check contains info about sender as expected */
        String expectedSenderClassname = simulatedCaller.getClass().getName();
        String senderClassName = inspection123.getSenderClassName();
        assertEquals(expectedSenderClassname, senderClassName);// test calls the inspection

    }

}
