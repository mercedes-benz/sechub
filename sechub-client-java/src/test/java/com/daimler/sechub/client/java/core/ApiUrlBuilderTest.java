package com.daimler.sechub.client.java.core;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class ApiUrlBuilderTest {
    @Test
    public void build_url_is_alive() {
        int port = 5935;
        String hostname = "localhost";
        String protocol = "https";

        URI uri = null;

        try {
            uri = new ApiUrlBuilder(protocol, hostname, port).buildCheckIsAliveUrl();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assertEquals("https://localhost:5935/api/anonymous/check/alive", uri.toString());
    }

    @Test
    public void build_url_get_job_report_url() {
        int port = 5935;
        String hostname = "localhost";
        String protocol = "http";
        String projectID = "myproject";
        UUID jobUUID = UUID.fromString("d2fe6430-a47c-494c-a794-761b4d33d090");

        URI uri = null;

        try {
            uri = new ApiUrlBuilder(protocol, hostname, port).buildGetJobReportUrl(projectID, jobUUID);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assertEquals("http://localhost:5935/api/project/myproject/report/d2fe6430-a47c-494c-a794-761b4d33d090", uri.toString());
    }
}
