package com.mercedesbenz.sechub.xraywrapper.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;

import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIResponse;

public class XrayFullResponseBuilder {

    // https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-networking-2/src/main/java/com/baeldung/httprequest/FullResponseBuilder.java
    public static XrayAPIResponse getFullResponse(HttpURLConnection con, String filename) throws IOException {
        XrayAPIResponse response = new XrayAPIResponse();
        filename = filename + ".zip";

        response.setStatus_code(con.getResponseCode());

        // append headers
        Map<String, List<String>> header = con.getHeaderFields();
        response.setHeaders(header);

        // append response
        InputStream is = null;

        if (con.getResponseCode() > 299) {
            is = con.getErrorStream();
        } else {
            is = con.getInputStream();
        }

        if (is == null) {
            // todo: Error Log or Error Handling
            System.out.println("Input Stream is empty - an error occured. Status Code:" + response.getStatus_code());
            System.out.println("Properties:" + con.getRequestProperties().toString());
            System.exit(0);
        }

        String type = con.getHeaderField("Content-Type");
        if (Objects.equals(type, "application/gzip")) {
            // case application/gzip (report files in zip container)
            response.saveZipFile(filename, is);
        } else {
            // case application/json is saved as string body
            StringBuilder content = response.saveJsonBody(is);
            response.setBody(content.toString());
        }
        return response;
    }
}
