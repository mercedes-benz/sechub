// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.adapter.springextension.ResourceHttpMessageConverterHandlingInputStreams;
import com.mercedesbenz.sechub.adapter.support.TrustAllSupport;

/**
 * Context for REST execution per spring REST templates (per default with a
 * simple String as result).
 *
 * @author Albert Tregnaghi
 *
 */
public abstract class AbstractSpringRestAdapterContext<C extends AdapterConfig, A extends Adapter<C>> extends AbstractAdapterContext<C, A> {

    private RestTemplate restTemplate;

    public AbstractSpringRestAdapterContext(C config, A adapter, AdapterRuntimeContext runtimeContext) {
        super(config, adapter, runtimeContext);
        /* setup dedicated rest template */

        ClientHttpRequestFactory requestFactory = createRequestFactory(config);

        restTemplate = new RestTemplate(requestFactory);

        restTemplate.getMessageConverters().addAll(createMessageConverters());

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new TraceLogClientHTTPRequestInterceptor());
        ClientHttpRequestInterceptor interceptor = createInterceptorOrNull(config);
        if (interceptor != null) {
            interceptors.add(interceptor);
        }

        Object obj = config.getOptions().get(AdapterOptionKey.CLIENT_HTTP_REQUEST_INTERCEPTOR);
        if (obj instanceof ClientHttpRequestInterceptor) {
            ClientHttpRequestInterceptor optionInterceptor = (ClientHttpRequestInterceptor) obj;
            interceptors.add(optionInterceptor);
        }
        restTemplate.setInterceptors(interceptors);

        handleSpringExtensions();

    }

    private void handleSpringExtensions() {
        handleLongInputStreams();
    }

    private void handleLongInputStreams() {
        if (!enableResourceHttpMessageConverterHandlingInputStream()) {
            return;
        }

        // see https://github.com/spring-projects/spring-framework/issues/18147
        List<HttpMessageConverter<?>> messageConverters = this.restTemplate.getMessageConverters();
        for (int i = 0; i < messageConverters.size(); i++) {
            HttpMessageConverter<?> messageConverter = messageConverters.get(i);
            if (messageConverter.getClass().equals(ResourceHttpMessageConverter.class))
                messageConverters.set(i, new ResourceHttpMessageConverterHandlingInputStreams());
        }
    }

    /**
     * When enabled a special version of message converter for resources http
     * messages will be used: This variant will provide support see
     * https://github.com/spring-projects/spring-framework/issues/18147
     *
     * @return <code>false</code> per default, can be overriden when needed
     */
    protected boolean enableResourceHttpMessageConverterHandlingInputStream() {
        /*
         * per default we use NOT the workaround but standard behavior - can be
         * overriden by children
         */
        return false;
    }

    private ClientHttpRequestFactory createRequestFactory(C config) {
        ClientHttpRequestFactory factory = null;
        if (!config.isTrustAllCertificatesEnabled()) {
            factory = createStandardSpringRequestFactory(config);
        } else {
            factory = new TrustAllSupport(getAdapter(), config).createTrustAllFactory();

        }
        /*
         * we create buffering variant, so we can do trace logging if necessary - see
         * TraceLogClientHTTPRequestInterceptor
         */
        return new BufferingClientHttpRequestFactory(factory);
    }

    private HttpComponentsClientHttpRequestFactory createStandardSpringRequestFactory(C config) {
        HttpClientBuilder builder = HttpClientBuilder.create().useSystemProperties();
        if (config.isProxyDefined()) {
            HttpHost proxy = new HttpHost(config.getProxyHostname(), config.getProxyPort());
            builder.setProxy(proxy);
        }
        HttpClient httpClient = builder.build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    private Set<HttpMessageConverter<?>> createMessageConverters() {
        Set<HttpMessageConverter<?>> set = new HashSet<>();
        addClientHttpRequestInterceptors(set);
        return set;
    }

    /**
     * Adds the message converters to use, can be overridden. per default a
     * {@link StringHttpMessageConverter} is added
     */
    protected void addClientHttpRequestInterceptors(Set<HttpMessageConverter<?>> set) {
        set.add(new StringHttpMessageConverter());
    }

    protected abstract ClientHttpRequestInterceptor createInterceptorOrNull(C config);

    @Override
    public RestOperations getRestOperations() {
        return restTemplate;
    }

}
