// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.wrapper.xray.util.IOHelper;
import com.mercedesbenz.sechub.wrapper.xray.util.ZipFileCreator;

public class XrayAPIResponseFactory {

    /**
     * extracts response from http url connection code similar to @see <a href=
     * "https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-networking-2/src/main/java/com/baeldung/httprequest/FullResponseBuilder.java"/a>
     *
     * @param httpURLConnection httpUrl connection
     * @param zipArchive        file name to save zip content
     * @return xray api http response
     * @throws XrayWrapperException
     */
    public XrayAPIResponse createHttpResponseFromConnection(HttpURLConnection httpURLConnection, String zipArchive) throws XrayWrapperException {
        File zipFileArchive = new File(zipArchive + ".zip");

        // read response code
        int responseCode = 0;
        try {
            responseCode = httpURLConnection.getResponseCode();
        } catch (IOException e) {
            throw new XrayWrapperException("Could not get response code from HTTP connection.", XrayWrapperExitCode.IO_ERROR, e);
        }

        // red response Message
        String responseMessage;
        try {
            responseMessage = httpURLConnection.getResponseMessage();
        } catch (IOException e) {
            throw new XrayWrapperException("Could not read Response Message from HTTP connection.", XrayWrapperExitCode.IO_ERROR, e);
        }

        // read headers
        Map<String, List<String>> header = httpURLConnection.getHeaderFields();

        // get input stream from response, client accept status codes 3xx
        String content = "";
        if (responseCode > 399) {
            try (InputStream inputStream = httpURLConnection.getErrorStream()) {
                if (inputStream != null) {
                    if (isZipBody(httpURLConnection)) {
                        saveHTTPContentToZip(zipFileArchive, inputStream);
                    } else {
                        content = readHTTPContentAsString(inputStream);
                    }
                }
            } catch (IOException e) {
                throw new XrayWrapperException("Could not save https error stream", XrayWrapperExitCode.IO_ERROR, e);
            }
        } else {
            try (InputStream inputStream = httpURLConnection.getInputStream()) {
                if (inputStream != null) {
                    if (isZipBody(httpURLConnection)) {
                        saveHTTPContentToZip(zipFileArchive, inputStream);
                    } else {
                        content = readHTTPContentAsString(inputStream);
                    }
                }
            } catch (IOException e) {
                throw new XrayWrapperException("Could not save https input stream", XrayWrapperExitCode.IO_ERROR, e);
            }
        }
        // if input stream is null, we return a response with empty body
        // null input-stream occurred during tests with Xray Artifactory, but still had
        // message and statuscode
        return XrayAPIResponse.Builder.builder().httpStatusCode(responseCode).headers(header).addResponseBody(content).addResponseMessage(responseMessage)
                .build();
    }

    private boolean isZipBody(HttpURLConnection httpURLConnection) {
        String type = httpURLConnection.getHeaderField("Content-Type");
        // server returns application/gzip as body which needs to be stored in zip file
        // or server returns application/json body which can be saved as string body
        return Objects.equals(type, "application/gzip");
    }

    private String readHTTPContentAsString(InputStream inputStream) throws XrayWrapperException {
        IOHelper ioHelper = new IOHelper();
        return ioHelper.readInputStreamAsString(inputStream);
    }

    private void saveHTTPContentToZip(File zipFileArchive, InputStream inputStream) throws XrayWrapperException {
        ZipFileCreator zipFileCreator = new ZipFileCreator();
        zipFileCreator.createZipFromZipInputStream(zipFileArchive, inputStream);
    }
}