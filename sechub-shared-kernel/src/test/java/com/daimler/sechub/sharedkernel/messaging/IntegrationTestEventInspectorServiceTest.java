package com.daimler.sechub.sharedkernel.messaging;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

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
        
        simulatedDomainMessageService = new SimulatedDomainMessageService();
        simulatedCaller = new SimulatedCaller();
        
        
        request = mock(DomainMessage.class);
        when(request.getMessageId()).thenReturn(MessageID.PROJECT_CREATED);
        asynchronousMessagerHandler = mock(AsynchronMessageHandler.class);
        synchronousMessagerHandler = mock(SynchronMessageHandler.class);
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
        simulatedCaller.simulateCallerSendAsync(123);
        serviceToTest.inspectReceiveAsynchronMessage(request, 123, asynchronousMessagerHandler);
        
        /* test */
        IntegrationTestEventHistory history = serviceToTest.getHistory();
        assertNotNull(history);
        Map<Integer, IntegrationTestEventHistoryInspection> idToInspectionMap = history.getIdToInspectionMap();
        assertEquals(1, idToInspectionMap.keySet().size());
        
        
        /* check inspection id as expected */
        Entry<Integer, IntegrationTestEventHistoryInspection> entry = idToInspectionMap.entrySet().iterator().next();
        assertEquals(Integer.valueOf(123),entry.getKey());
        
        IntegrationTestEventHistoryInspection inspection123 = entry.getValue();
        assertEquals(false, inspection123.isSynchron());
        
        /* check contains info about receiver as expected */    
        List<String> receiverClassNames = inspection123.getReceiverClassNames();
        assertEquals(1, receiverClassNames.size());
        assertEquals(asynchronousMessagerHandler.getClass().getName(), receiverClassNames.iterator().next());

        /* check contains info about sender as expected */
        assertEquals(simulatedCaller.getClass().getName(), inspection123.getSenderClassName());// test calls the inspection
        
    }
    
    @Test
    public void sync_when_initialized_with_usecase_UC_aDMIN_CREATE_PROJECT_returns_history_without_usecasename() {
        /* execute */
        simulatedCaller.simulateCallereSendSync(123);
        serviceToTest.inspectReceiveSynchronMessage(request, 123, synchronousMessagerHandler);
        
        /* test */
        IntegrationTestEventHistory history = serviceToTest.getHistory();
        assertNotNull(history);
        Map<Integer, IntegrationTestEventHistoryInspection> idToInspectionMap = history.getIdToInspectionMap();
        assertEquals(1, idToInspectionMap.keySet().size());
        
        
        /* check inspection id as expected */
        Entry<Integer, IntegrationTestEventHistoryInspection> entry = idToInspectionMap.entrySet().iterator().next();
        assertEquals(Integer.valueOf(123),entry.getKey());
        
        IntegrationTestEventHistoryInspection inspection123 = entry.getValue();
        assertEquals(true, inspection123.isSynchron());

        /* check contains info about receiver as expected */    
        List<String> receiverClassNames = inspection123.getReceiverClassNames();
        assertEquals(1, receiverClassNames.size());
        assertEquals(synchronousMessagerHandler.getClass().getName(), receiverClassNames.iterator().next());

        /* check contains info about sender as expected */
        assertEquals(simulatedCaller.getClass().getName(), inspection123.getSenderClassName());// test calls the inspection
        
    }
    
    /**
     * Simulation to check if stacktrace caller identification works - only necessary for sender 
     * @author Albert Tregnaghi
     *
     */
    private class SimulatedCaller{
        
        public void simulateCallerSendAsync(int inspectId) {
            simulatedDomainMessageService.simulateServiceSendAsync(inspectId);
        }
        public void simulateCallereSendSync(int inspectId) {
            simulatedDomainMessageService.simulateServiceSendSync(inspectId);
        }
    }
    
    private class SimulatedDomainMessageService{
        public void simulateServiceSendAsync(int inspectId) {
            serviceToTest.inspectSendAsynchron(request, inspectId);
        }
        public void simulateServiceSendSync(int inspectId) {
            serviceToTest.inspectSendSynchron(request, inspectId);
        }
    }
    
    

}
