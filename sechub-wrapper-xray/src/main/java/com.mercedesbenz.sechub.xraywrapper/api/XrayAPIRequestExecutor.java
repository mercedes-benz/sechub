package com.mercedesbenz.sechub.xraywrapper.api;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;
import com.mercedesbenz.sechub.xraywrapper.util.XrayAPIAuthenticationHeader;

public class XrayAPIRequestExecutor {

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
    public static HttpURLConnection setUpGetConnection(XrayAPIRequest request) throws XrayWrapperRuntimeException {
        URL url;
        try {
            url = request.getUrl();
        } catch (MalformedURLException e) {
            throw new XrayWrapperRuntimeException("Url could not be parsed from String", e, XrayWrapperExitCode.MALFORMED_URL);
        }
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
                throw new XrayWrapperRuntimeException("Could not get Encrypt api request", e, XrayWrapperExitCode.UNSUPPORTED_ENCRYPTION);
            }
            try {
                os.write(input, 0, input.length);
            } catch (IOException e) {
                throw new XrayWrapperRuntimeException("Could not write Output Stream for api connection", e, XrayWrapperExitCode.IO_ERROR);
            }
            return con;
        } else {
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            try {
                con.connect();
            } catch (IOException e) {
                throw new XrayWrapperRuntimeException("Could not open api connection", e, XrayWrapperExitCode.IO_ERROR);
            }
            return con;
        }
    }
}
