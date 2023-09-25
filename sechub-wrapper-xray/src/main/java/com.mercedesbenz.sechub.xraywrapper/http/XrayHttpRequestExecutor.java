package com.mercedesbenz.sechub.xraywrapper.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.mercedesbenz.sechub.xraywrapper.util.XrayAuthenticationHeader;

public class XrayHttpRequestExecutor {

    static String authenticate() {
        return XrayAuthenticationHeader.buildAuthHeader();
    }

    /**
     * Creates and Http get connection and sends request
     *
     * @throws IOException
     */
    public static HttpURLConnection setUpGetConnection(XrayAPIRequest request) throws IOException {
        URL url = request.getUrl();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(request.getRequestMethodEnum().toString());
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        if (request.needAuthentication()) {
            String auth = authenticate();
            con.setRequestProperty("Authorization", auth);
        }
        if (request.getRequestMethodEnum() == XrayAPIRequest.RequestMethodEnum.POST) {
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            byte[] input = request.getData().getBytes("utf-8");
            os.write(input, 0, input.length);
            return con;
        } else {
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.connect();
            return con;
        }
    }
}
