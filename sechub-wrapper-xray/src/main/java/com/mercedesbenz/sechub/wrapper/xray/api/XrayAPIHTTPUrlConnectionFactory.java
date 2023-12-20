// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.wrapper.xray.util.XrayAPIAuthenticationHeader;

public class XrayAPIHTTPUrlConnectionFactory {

    private static final int CONNECTION_TIMEOUT_IN_MILLISECONDS = 5000;

    /**
     * Creates an httpURL connection to the jfrog server
     *
     * @param request https API request
     * @return https connection
     * @throws XrayWrapperException
     */
    public HttpURLConnection create(XrayAPIRequest request) throws XrayWrapperException {
        URL url = request.getUrl();
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new XrayWrapperException("Could not open https connection", XrayWrapperExitCode.IO_ERROR, e);
        }

        addHTTPParameters(connection, request.getRequestMethodEnum().toString());
        addAuthentication(connection, request.isAuthenticationNeeded());

        if (request.getRequestMethodEnum() == XrayAPIRequest.RequestMethodEnum.POST) {
            createHTTPPOSTConnection(connection, request);
        } else {
            createHTTPConnection(connection);
        }
        return connection;
    }

    private void addHTTPParameters(HttpURLConnection connection, String method) throws XrayWrapperException {
        try {
            connection.setRequestMethod(method);
        } catch (ProtocolException e) {
            throw new XrayWrapperException("Protocol method is invalid", XrayWrapperExitCode.INVALID_HTTP_REQUEST, e);
        }
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
    }

    private void addAuthentication(HttpURLConnection connection, boolean requiresAuthentication) throws XrayWrapperException {
        if (requiresAuthentication) {
            String authHeader = XrayAPIAuthenticationHeader.buildBasicAuthHeader();
            connection.setRequestProperty("Authorization", authHeader);
        }
    }

    private void createHTTPConnection(HttpURLConnection connection) throws XrayWrapperException {
        connection.setConnectTimeout(CONNECTION_TIMEOUT_IN_MILLISECONDS);
        connection.setReadTimeout(CONNECTION_TIMEOUT_IN_MILLISECONDS);
        try {
            connection.connect();
        } catch (IOException e) {
            throw new XrayWrapperException("Could not open connection to Artifactory", XrayWrapperExitCode.IO_ERROR, e);
        }
    }

    private static void createHTTPPOSTConnection(HttpURLConnection connection, XrayAPIRequest request) throws XrayWrapperException {
        connection.setDoOutput(true);
        OutputStream outputStream;
        try {
            outputStream = connection.getOutputStream();
        } catch (IOException e) {
            throw new XrayWrapperException("Could not get Output Stream for api connection", XrayWrapperExitCode.IO_ERROR, e);
        }
        byte[] input;
        input = request.getData().getBytes(StandardCharsets.UTF_8);
        try {
            outputStream.write(input, 0, input.length);
        } catch (IOException e) {
            throw new XrayWrapperException("Could not write Output Stream for api connection", XrayWrapperExitCode.IO_ERROR, e);
        }
    }
}
