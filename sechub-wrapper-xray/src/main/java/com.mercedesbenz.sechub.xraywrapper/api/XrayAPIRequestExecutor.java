package com.mercedesbenz.sechub.xraywrapper.api;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;
import com.mercedesbenz.sechub.xraywrapper.util.XrayAPIAuthenticationHeader;

public class XrayAPIRequestExecutor {

    private static final int CONNECTION_TIMEOUT_IN_MILLISECONDS = 5000;

    static String authenticate() {
        return XrayAPIAuthenticationHeader.buildAuthHeader();
    }

    /**
     * sets up an httpURL connection to the jfrog server
     *
     * @param request https API request
     * @return https connection
     * @throws XrayWrapperRuntimeException
     */
    public static HttpURLConnection setUpHTTPConnection(XrayAPIRequest request) throws XrayWrapperRuntimeException {
        URL url = request.getUrl();
        HttpURLConnection con = null;

        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new XrayWrapperRuntimeException("Could not open https connection", e, XrayWrapperExitCode.IO_ERROR);
        }
        try {
            con.setRequestMethod(request.getRequestMethodEnum().toString());
        } catch (ProtocolException e) {
            throw new XrayWrapperRuntimeException("Protocol method is invalid", e, XrayWrapperExitCode.INVALID_HTTP_REQUEST);
        }
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");

        if (request.needAuthentication()) {
            String auth = authenticate();
            con.setRequestProperty("Authorization", auth);
        }
        if (request.getRequestMethodEnum() == XrayAPIRequest.RequestMethodEnum.POST) {
            con.setDoOutput(true);
            // todo: test if cbr works ?
            setUpPostConnection(con, request);
            return con;
        } else {
            con.setConnectTimeout(CONNECTION_TIMEOUT_IN_MILLISECONDS);
            con.setReadTimeout(CONNECTION_TIMEOUT_IN_MILLISECONDS);
            try {
                con.connect();
            } catch (IOException e) {
                throw new XrayWrapperRuntimeException("Could not open api connection", e, XrayWrapperExitCode.IO_ERROR);
            }
            return con;
        }
    }

    private static void setUpPostConnection(HttpURLConnection con, XrayAPIRequest request) {
        OutputStream os;
        try {
            os = con.getOutputStream();
        } catch (IOException e) {
            throw new XrayWrapperRuntimeException("Could not get Output Stream for api connection", e, XrayWrapperExitCode.IO_ERROR);
        }
        byte[] input;
        try {
            input = request.getData().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new XrayWrapperRuntimeException("Could not parse request: ", e, XrayWrapperExitCode.UNSUPPORTED_ENCODING);
        }
        try {
            os.write(input, 0, input.length);
        } catch (IOException e) {
            throw new XrayWrapperRuntimeException("Could not write Output Stream for api connection", e, XrayWrapperExitCode.IO_ERROR);
        }
    }
}
