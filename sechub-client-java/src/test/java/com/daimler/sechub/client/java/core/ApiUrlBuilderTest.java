package com.daimler.sechub.client.java.core;


import static org.junit.Assert.*;

import java.net.URI;
import java.util.UUID;

import org.junit.Test;

public class ApiUrlBuilderTest {
    
    @Test
    public void build_url_is_alive() throws Exception {
        /* prepare */
        int port = 5935;
        String hostname = "localhost";
        String protocol = "https";

        /* execute */
        URI uri = new ApiUrlBuilder(protocol, hostname, port).buildCheckIsAliveUrl();

        /* test */
        assertEquals("https://localhost:5935/api/anonymous/check/alive", uri.toString());
    }

    @Test
    public void build_url_get_job_report_url() throws Exception {
        /* prepare */
        int port = 5935;
        String hostname = "localhost";
        String protocol = "http";
        String projectID = "myproject";
        UUID jobUUID = UUID.fromString("d2fe6430-a47c-494c-a794-761b4d33d090");

        /* execute */
        URI uri = new ApiUrlBuilder(protocol, hostname, port).buildGetJobReportUrl(projectID, jobUUID);

        /* test */
        assertEquals("http://localhost:5935/api/project/myproject/report/d2fe6430-a47c-494c-a794-761b4d33d090", uri.toString());
    }
}
