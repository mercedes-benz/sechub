// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.daimler.sechub.adapter.support.TrustAllSupport;

/**
 * Context for REST execution per spring REST templates (per default with a simple String as result).
 * 
 * @author Albert Tregnaghi
 *
 */
public abstract class AbstractSpringRestAdapterContext<C extends AdapterConfig, A extends Adapter<C>> extends AbstractAdapterContext<C,A> {

	public static final String SECHUB_OPTION_CLIENTHTTPREQUESTINTERCEPTOR = "sechub.option.clienthttprequestinterceptor";
	private RestTemplate restTemplate;

	public AbstractSpringRestAdapterContext(C config, A adapter) {
		super(config,adapter);
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
		
		Object obj = config.getOptions().get(SECHUB_OPTION_CLIENTHTTPREQUESTINTERCEPTOR);
		if (obj instanceof ClientHttpRequestInterceptor) {
			ClientHttpRequestInterceptor optionInterceptor = (ClientHttpRequestInterceptor) obj;
			interceptors.add(optionInterceptor);
		}
		restTemplate.setInterceptors(interceptors);

	}
	
	private ClientHttpRequestFactory createRequestFactory(C config) {
		ClientHttpRequestFactory factory = null;
		if (! config.isTrustAllCertificatesEnabled()) {
			factory = createStandardSpringRequestFactory(config);
		}else {
			factory = new TrustAllSupport(getAdapter(),config).createTrustAllFactory();
			
		}
		/* we create buffering variant, so we can do trace logging if necessary - see TraceLogClientHTTPRequestInterceptor*/
		return new BufferingClientHttpRequestFactory(factory);
	}

	private SimpleClientHttpRequestFactory createStandardSpringRequestFactory(C config) {
		/* use standard Spring way, so uses configured trusted certificates etc. etc. */
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		if (config.isProxyDefined()) {
			Proxy proxy = new Proxy(Type.SOCKS,
					new InetSocketAddress(config.getProxyHostname(), config.getProxyPort()));
			requestFactory.setProxy(proxy);
		}
		return requestFactory;
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
	public final RestOperations getRestOperations() {
		return restTemplate;
	}

}
