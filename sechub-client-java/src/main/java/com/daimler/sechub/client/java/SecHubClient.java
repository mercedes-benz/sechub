package com.daimler.sechub.client.java;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import com.daimler.sechub.client.java.core.ApiUrlBuilder;
import com.daimler.sechub.client.java.core.SecHubReportReadingException;
import com.daimler.sechub.client.java.report.SecHubReport;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SecHubClient {

    private String username;
    private String apiToken;
    private URI hostUri;
    private int hostPort;
    private boolean trustAll;

    public static SecHubClient create(String username, String apiToken, String hostUri, int hostPort) {
        return create(username, apiToken, hostUri, hostPort, false);
    }
    
    public static SecHubClient create(String username, String apiToken, String hostUri, int hostPort, boolean trustAll) {
        URI baseUri = null;
        
        try {
            baseUri = new URIBuilder(hostUri).build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Not an acceptable uri:"+hostUri,e);
        }
        return new SecHubClient(username, apiToken, baseUri, hostPort, trustAll);
    }

    private SecHubClient(String username, String apiToken, URI hostUri, int hostPort, boolean trustAll) {
        this.username = username;
        this.apiToken = apiToken;
        this.hostUri = hostUri;
        this.hostPort = hostPort;
        this.trustAll = trustAll;
    }


    public String getUsername() {
        return username;
    }

    public String getApiToken() {
        return apiToken;
    }

    public URI getHostUri() {
        return hostUri;
    }

    public int getHostPort() {
        return hostPort;
    }

    public boolean isTrustAll() {
        return trustAll;
    }

    public Boolean isAlive() {
        CloseableHttpClient httpClient = this.getHttpClient();

        Boolean isAlive = false;

        URI uri = null;

        try {
            uri = new ApiUrlBuilder(this.getHostUri().getScheme(), this.getHostUri().getHost(), this.hostPort).buildCheckIsAliveUrl();

        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HttpHead httpHead = new HttpHead(uri);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpHead);
        } catch (ClientProtocolException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            int status = response.getStatusLine().getStatusCode();

            if (HttpStatus.SC_OK == status) {
                isAlive = true;
            }
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return isAlive;
    }

    protected CloseableHttpClient getHttpClient() {
        CloseableHttpClient httpClient = null;

        if (this.isTrustAll()) {
            httpClient = this.getInsecureHttpClient();
        } else {
            httpClient = HttpClients.createDefault();
        }

        return httpClient;
    }

    private CloseableHttpClient getInsecureHttpClient() {
        SSLContext sslContext = null;

        try {
            sslContext = SSLContexts.custom().loadTrustMaterial((KeyStore) null, new TrustSelfSignedStrategy()).build();
        } catch (KeyManagementException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (NoSuchAlgorithmException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (KeyStoreException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();

        return httpClient;
    }

    public SecHubReport downloadReport(String project, UUID jobUUID) throws SecHubReportReadingException {
        SecHubReport report = null;
        CloseableHttpClient httpClient = this.getHttpClient();

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        credentialsProvider.setCredentials(new AuthScope(this.getHostUri().getHost(), this.getHostPort()),
                new UsernamePasswordCredentials(this.getUsername(), this.getApiToken()));

        HttpClientContext localContext = HttpClientContext.create();
        localContext.setCredentialsProvider(credentialsProvider);

        URI uri = null;

        try {
            uri = new ApiUrlBuilder(this.getHostUri().getScheme(), this.getHostUri().getHost(), this.hostPort).buildGetJobReportUrl(project, jobUUID);

        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HttpGet httpget = new HttpGet(uri);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpget, localContext);
        } catch (ClientProtocolException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            ResponseHandler<String> handler = new BasicResponseHandler();
            try {
                String body = handler.handleResponse(response);

                ObjectMapper mapper = new ObjectMapper();

                try {
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    report = mapper.readValue(body, SecHubReport.class);
                } catch (JsonParseException|JsonMappingException e) {
                    throw new SecHubReportReadingException("Parse problem while reading "+uri,e);
                }

            } catch (IOException e) {
                throw new SecHubReportReadingException("Parse problem while reading "+uri,e);
            }
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return report;
    }

    public static SecHubReport importSecHubJsonReport(File file) throws SecHubReportReadingException {
        SecHubReport report = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Do not fail on unknown properties
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            report = mapper.readValue(file, SecHubReport.class);
        } catch (JsonParseException|JsonMappingException e) {
            throw new SecHubReportReadingException(e);
        } catch (IOException e) {
            throw new SecHubReportReadingException(e);
        }

        return report;
    }
}
