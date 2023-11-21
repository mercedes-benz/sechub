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
        if (responseCode > 399) {
            try (InputStream inputStream = httpURLConnection.getErrorStream()) {
                if (inputStream != null) {
                    String content = saveOrReadHTTPContent(httpURLConnection, zipArchive, inputStream);
                    return XrayAPIResponse.Builder.builder().statusCode(responseCode).headers(header).addResponseBody(content)
                            .addResponseMessage(responseMessage).build();
                }
            } catch (IOException e) {
                throw new XrayWrapperException("Could not save https input stream to zip file", XrayWrapperExitCode.IO_ERROR, e);
            }

        } else {
            try (InputStream inputStream = httpURLConnection.getInputStream()) {
                String content = saveOrReadHTTPContent(httpURLConnection, zipArchive, inputStream);
                return XrayAPIResponse.Builder.builder().statusCode(responseCode).headers(header).addResponseBody(content).addResponseMessage(responseMessage)
                        .build();
            } catch (IOException e) {
                throw new XrayWrapperException("Could not save https input stream to zip file", XrayWrapperExitCode.IO_ERROR, e);
            }
        }

        return XrayAPIResponse.Builder.builder().statusCode(responseCode).headers(header).addResponseMessage(responseMessage).build();
    }

    private String saveOrReadHTTPContent(HttpURLConnection httpURLConnection, String zipArchive, InputStream inputStream) throws XrayWrapperException {
        File zipFileArchive = new File(zipArchive + ".zip");
        ZipFileCreator zipFileCreator = new ZipFileCreator();
        IOHelper streamStringReader = new IOHelper();
        String type = httpURLConnection.getHeaderField("Content-Type");
        if (Objects.equals(type, "application/gzip")) {
            // server returns application/gzip as body which needs to be stored in zip file
            zipFileCreator.zip(zipFileArchive, inputStream);
            return "";
        } else {
            // server returns application/json body which can be saved as string body
            return streamStringReader.readInputStreamAsString(inputStream);
        }
    }
}