package com.mercedesbenz.sechub.xraywrapper.api;

import static com.mercedesbenz.sechub.xraywrapper.util.InputStreamSaver.saveInputStreamToString;
import static com.mercedesbenz.sechub.xraywrapper.util.InputStreamSaver.saveInputStreamToZipFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;

public class XrayAPIResponseBuilder {

    /**
     * extracts response from http url connection code similar to @see <a href=
     * "https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-networking-2/src/main/java/com/baeldung/httprequest/FullResponseBuilder.java">...</a>
     *
     * @param con        httpUrl connection
     * @param zipArchive file name to save zip content
     * @return xray api http response
     * @throws XrayWrapperRuntimeException
     */
    public static XrayAPIResponse getHttpResponseFromConnection(HttpURLConnection con, String zipArchive) throws XrayWrapperRuntimeException {
        XrayAPIResponse response = new XrayAPIResponse();
        zipArchive = zipArchive + ".zip";
        int responseCode = 0;

        // read response code
        try {
            responseCode = con.getResponseCode();
        } catch (IOException e) {
            throw new XrayWrapperRuntimeException("Could not get response code from api connection.", e, XrayWrapperExitCode.IO_ERROR);
        }
        response.setStatus_code(responseCode);

        // append headers
        Map<String, List<String>> header = con.getHeaderFields();
        response.setHeaders(header);

        // append response body
        InputStream is;

        if (responseCode > 299) {
            is = con.getErrorStream();
        } else {
            try {
                is = con.getInputStream();
            } catch (IOException e) {
                throw new XrayWrapperRuntimeException("Could not get Input stream from api connection.", e, XrayWrapperExitCode.IO_ERROR);
            }
        }

        if (is != null) {
            String type = con.getHeaderField("Content-Type");
            if (Objects.equals(type, "application/gzip")) {
                // case application/gzip (report files in zip container)
                saveInputStreamToZipFile(zipArchive, is);
            } else {
                // case application/json is saved as string body
                String content = saveInputStreamToString(is);
                response.setBody(content);
            }
        }
        return response;
    }
}
