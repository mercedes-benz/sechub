package com.mercedesbenz.sechub.xraywrapper.http;

import static com.mercedesbenz.sechub.xraywrapper.util.InputStreamSaver.saveInputStreamToStringBuilder;
import static com.mercedesbenz.sechub.xraywrapper.util.InputStreamSaver.saveInputStreamToZipFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class XrayHttpResponseBuilder {

    // https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-networking-2/src/main/java/com/baeldung/httprequest/FullResponseBuilder.java

    /**
     * Get input stream from connection and builds Xray response from it
     *
     * @param con
     * @param zipArchive
     * @return
     * @throws IOException
     */
    public static XrayAPIResponse getHttpResponseFromConnection(HttpURLConnection con, String zipArchive) throws IOException {
        XrayAPIResponse response = new XrayAPIResponse();
        zipArchive = zipArchive + ".zip";

        response.setStatus_code(con.getResponseCode());

        // append headers
        Map<String, List<String>> header = con.getHeaderFields();
        response.setHeaders(header);

        // append response
        InputStream is;

        if (con.getResponseCode() > 299) {
            is = con.getErrorStream();
        } else {
            is = con.getInputStream();
        }

        if (is != null) {
            String type = con.getHeaderField("Content-Type");
            if (Objects.equals(type, "application/gzip")) {
                // case application/gzip (report files in zip container)
                saveInputStreamToZipFile(zipArchive, is);
            } else {
                // case application/json is saved as string body
                String content = saveInputStreamToStringBuilder(is);
                response.setBody(content);
            }
        }
        return response;
    }
}