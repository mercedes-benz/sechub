package com.mercedesbenz.sechub.wrapper.xray.api;

import static com.mercedesbenz.sechub.wrapper.xray.util.InputStreamSaver.readInputStreamAsString;
import static com.mercedesbenz.sechub.wrapper.xray.util.InputStreamSaver.saveInputStreamToZipFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperRuntimeException;

public class XrayAPIResponseBuilder {

    /**
     * extracts response from http url connection code similar to @see <a href=
     * "https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-networking-2/src/main/java/com/baeldung/httprequest/FullResponseBuilder.java"/a>
     *
     * @param httpURLConnection httpUrl connection
     * @param zipArchive        file name to save zip content
     * @return xray api http response
     * @throws XrayWrapperRuntimeException
     */
    public static XrayAPIResponse getHttpResponseFromConnection(HttpURLConnection httpURLConnection, String zipArchive) throws XrayWrapperRuntimeException {
        zipArchive = zipArchive + ".zip";

        // read response code
        int responseCode = 0;
        try {
            responseCode = httpURLConnection.getResponseCode();
        } catch (IOException e) {
            throw new XrayWrapperRuntimeException("Could not get response code from HTTP connection.", e, XrayWrapperExitCode.IO_ERROR);
        }

        // red response Message
        String responseMessage;
        try {
            responseMessage = httpURLConnection.getResponseMessage();
        } catch (IOException e) {
            throw new XrayWrapperRuntimeException("Could not read Response Message from HTTP connection.", e, XrayWrapperExitCode.IO_ERROR);
        }

        // read headers
        Map<String, List<String>> header = httpURLConnection.getHeaderFields();

        // get input stream from response, client accept status codes 3xx
        InputStream inputStream;
        if (responseCode > 399) {
            inputStream = httpURLConnection.getErrorStream();
        } else {
            try {
                inputStream = httpURLConnection.getInputStream();
            } catch (IOException e) {
                throw new XrayWrapperRuntimeException("Could not get Input stream from HTTP connection.", e, XrayWrapperExitCode.IO_ERROR);
            }
        }

        // read content from input stream
        String content;
        if (inputStream != null) {
            String type = httpURLConnection.getHeaderField("Content-Type");
            if (Objects.equals(type, "application/gzip")) {
                // server returns application/gzip as body which needs to be stored in zip file
                saveInputStreamToZipFile(zipArchive, inputStream);
                content = "";
            } else {
                // server returns application/json body which can be saved as string body
                content = readInputStreamAsString(inputStream);
            }
            return XrayAPIResponse.Builder.create(responseCode, header).setBody(content).setResponseMessage(responseMessage).build();
        }
        return XrayAPIResponse.Builder.create(responseCode, header).setResponseMessage(responseMessage).build();
    }
}
