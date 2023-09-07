package com.mercedesbenz.sechub.xraywrapper.helper;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class XrayAPIResponse {

    private int status_code;

    private Map<String, List<String>> headers;

    private String body;

    public XrayAPIResponse() {
        status_code = 0;
        headers = Collections.emptyMap();
        body = "";
    }

    public int getStatus_code() {
        return status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void print() {
        System.out.println("HTTPS status code: " + status_code);
        System.out.println("Headers: " + headers.toString());
        System.out.println("Body: " + body);
    }

    public void saveZipFile(String filename, InputStream is) throws IOException {
        File file = new File(filename);
        int read;
        byte[] buffer = new byte[1024];
        FileOutputStream fstream = new FileOutputStream(file);
        while ((read = is.read(buffer)) != -1) {
            fstream.write(buffer, 0, read);
        }
        is.close();
    }

    public StringBuilder saveJsonBody(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        is.close();
        return content;
    }
}
