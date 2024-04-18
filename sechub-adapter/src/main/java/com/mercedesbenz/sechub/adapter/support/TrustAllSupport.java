// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.support;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import com.mercedesbenz.sechub.adapter.Adapter;
import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.TrustAllConfig;

/**
 * This support works with SOCKS-Proxys V4.
 *
 * @author Albert Tregnaghi
 *
 */
public class TrustAllSupport {

    private static final String TLS = "TLS";
    private Adapter<?> adapter;
    private TrustAllConfig config;

    public TrustAllSupport(Adapter<?> adapter, TrustAllConfig config) {
        if (adapter == null) {
            throw new IllegalArgumentException("adapter may not be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("config may not be null");
        }
        this.adapter = adapter;
        this.config = config;
    }

    public ClientHttpRequestFactory createTrustAllFactory() {
        HttpClientBuilder clientBuilder = HttpClients.custom();
        SSLContext sslContext = null;

        if (config.isTrustAllCertificatesEnabled()) {
            try {
                sslContext = createTrustAllSSLContext(adapter);
            } catch (AdapterException e) {
                throw new IllegalStateException("Should not happen! See trace", e);
            }
        } else {
            sslContext = SSLContexts.createSystemDefault();
        }

        if (config.isProxyDefined()) {
            // HttP client proxy with socks does not working with standard HTTPHost,
            // clientBuilder.setProxy(..)
            // So own approach necessary, details see
            // https://stackoverflow.com/questions/22937983/how-to-use-socks-5-proxy-with-apache-http-client-4
            /* @formatter:off */
                SocksProxySSLConnectionSocketFactory socksProxySSLSocketFactory = new SocksProxySSLConnectionSocketFactory(sslContext);
                PoolingHttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create().
                        setSSLSocketFactory(socksProxySSLSocketFactory).
                        setDnsResolver(new FakeDnsResolver()).
                        build();
                /* @formatter:on */
            clientBuilder.setConnectionManager(cm);

        } else {

            /* formatter:off */
            SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create().setSslContext(sslContext)
                    .setHostnameVerifier(new NoopHostnameVerifier()).build();
            /* formatter:on */

            HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(sslSocketFactory).build();
            clientBuilder.setConnectionManager(connectionManager);
        }

        CloseableHttpClient httpClient = clientBuilder.build();

        HttpComponentsClientHttpRequestFactory requestFactory2 = new HttpComponentsClientHttpRequestFactory();
        requestFactory2.setHttpClient(httpClient);
        return requestFactory2;
    }

    private SSLContext createTrustAllSSLContext(Adapter<?> adapter) throws AdapterException {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance(TLS);

            TrustManager tm = new X509TrustManager() {

                private X509Certificate[] emptyCertificatesArray = new X509Certificate[] {};

                public void /* NOSONAR */ checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    /* we do not check the client - we trust all */
                }

                public void /* NOSONAR */ checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    /* we do not check the server - we trust all */
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return emptyCertificatesArray;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);

            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw adapter.asAdapterException("Was not able to initialize a trust all ssl context", e, config);
        }

    }

    private class SocksProxySSLConnectionSocketFactory extends SSLConnectionSocketFactory {

        public SocksProxySSLConnectionSocketFactory(final SSLContext sslContext) {
            // You may need this verifier if target site's certificate is not secure
            super(sslContext, NoopHostnameVerifier.INSTANCE);
        }

        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksaddr = new InetSocketAddress(config.getProxyHostname(), config.getProxyPort());
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        }

        @Override
        public Socket connectSocket(TimeValue connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress,
                HttpContext context) throws IOException {
            // Convert address to unresolved
            InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());
            return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
        }
    }

    private class FakeDnsResolver implements DnsResolver {
        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            // Return some fake DNS record for every request, we won't be using it
            return new InetAddress[] { InetAddress.getByAddress(new byte[] { 1, 1, 1, 1 }) };
        }

        @Override
        public String resolveCanonicalHostname(String host) throws UnknownHostException {
            return "fakeHost";
        }

    }
}