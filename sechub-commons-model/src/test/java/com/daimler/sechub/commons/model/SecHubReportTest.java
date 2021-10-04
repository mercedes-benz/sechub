package com.daimler.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Set;

import org.junit.jupiter.api.Test;

class SecHubReportTest {

    @Test
    void a_report_without_status_and_messages_can_be_read() {
        /* prepare */
        String jsonNoStatusOrMessages = TestFileReader.loadTextFile(new File("./src/test/resources/report/sechub-testreport1.json"), "\n");

        /* execute */
        SecHubReportModel report = SecHubReportModel.fromJSONString(jsonNoStatusOrMessages);
        
        /* test */
        assertNotNull(report);
        assertNull(report.getStatus());
        assertEquals(TrafficLight.GREEN, report.getTrafficLight());
        assertFalse(report.hasFailed());
        
        Set<SecHubMessage> messages = report.getMessages();
        assertNotNull(messages);
        assertTrue(messages.isEmpty());
    }
    
    @Test
    void a_report_with_status_failed_and_messages_can_be_read() {
        /* prepare */
        String jsonWithStatusAndMessages = TestFileReader.loadTextFile(new File("./src/test/resources/report/sechub-testreport2.json"), "\n");

        /* execute */
        SecHubReportModel report = SecHubReportModel.fromJSONString(jsonWithStatusAndMessages);
        
        /* test */
        assertNotNull(report);
        assertEquals(TrafficLight.GREEN, report.getTrafficLight());
        assertEquals(SecHubStatus.FAILED, report.getStatus());
        assertTrue(report.hasFailed());
        
        Set<SecHubMessage> messages = report.getMessages();
        assertNotNull(messages);
        assertFalse(messages.isEmpty());
        assertEquals(1, messages.size());
        SecHubMessage message = messages.iterator().next();
        
        assertEquals(SecHubMessageType.ERROR,message.getType());
        assertEquals("The code scan execution did fail!",message.getText());
        
    }

}
