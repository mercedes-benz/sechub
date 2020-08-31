// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.ArrayList;
import java.util.List;

public class WiremockUrlHistory {
    private List<String> postURLs = new ArrayList<>();
    private List<String> getURLs = new ArrayList<>();
    private List<String> putURLs = new ArrayList<>();
    private List<String> deleteURLs = new ArrayList<>();
    
    public String rememberPOST(String url){
        postURLs.add(url);
        return url;
    }
    public String rememberGET(String url){
        getURLs.add(url);
        return url;
    }
    public String rememberPUT(String url){
        putURLs.add(url);
        return url;
    }
    public String rememberDELETE(String url) {
        deleteURLs.add(url);
        return url;
    }
    
    public void assertAllRememberedUrlsWereRequested() {
        for (String postURL: postURLs) {
            verify(postRequestedFor(urlEqualTo(postURL)));
        }
        for (String getURL: getURLs) {
            verify(getRequestedFor(urlEqualTo(getURL))); 
        }
        for (String putURL: putURLs) {
            verify(putRequestedFor(urlEqualTo(putURL))); 
        }
        for (String getURL: deleteURLs) {
            verify(deleteRequestedFor(urlEqualTo(getURL))); 
        }
    }
}
