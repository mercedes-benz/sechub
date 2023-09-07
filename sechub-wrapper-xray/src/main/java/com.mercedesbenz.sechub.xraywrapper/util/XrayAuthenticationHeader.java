package com.mercedesbenz.sechub.xraywrapper.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class XrayAuthenticationHeader {
    public static String setAuthHeader() {
        String username = System.getenv("XRAY_USERNAME");
        String pwd = System.getenv("XRAY_PASSWORD");
        String auth = (username + ":" + pwd);
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }
}
