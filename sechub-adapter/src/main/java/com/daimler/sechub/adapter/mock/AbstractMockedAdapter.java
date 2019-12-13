// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.daimler.sechub.adapter.AbstractAdapter;
import com.daimler.sechub.adapter.AdapterConfig;
import com.daimler.sechub.adapter.AdapterContext;
import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.support.MockSupport;
/**
 * Abstract base class for mocked adapters. Will rely on {@link MockedAdapterSetupService} to support automated results depending on target urls...
 * So the adapter is configurable by a config file! For demo mode and integration testing this is very nice, we just have to setup the path to config file!
 * @author Albert Tregnaghi
 *
 * @param <A>
 * @param <C>
 */
public abstract class AbstractMockedAdapter<A extends AdapterContext<C>,C extends AdapterConfig> extends AbstractAdapter<A,C> implements MockedAdapter<C>{

	private static final Logger LOG = LoggerFactory.getLogger(AbstractMockedAdapter.class);
	final MockSupport mockSupport = new MockSupport();

	@Autowired
	private MockedAdapterSetupService setupService;

	@Override
	protected final String getAPIPrefix() {
		return "mockAdapterAPICall";
	}

	public final String start(C config) throws AdapterException {
		long timeStarted = System.currentTimeMillis();

		MockedAdapterSetupEntry setup = setupService.getSetupFor(createAdapterId());
		if (setup==null) {
			LOG.info("did not found adapter setup so returning empty string");
			return "";
		}
		String target = config.getTargetAsString();

		throwExceptionIfConfigured(config, setup, target);
		String result = loadResultAsConfigured(setup, target);

		waitIfConfigured(timeStarted, setup, target);

		LOG.trace("Returning content:{}",result);

		return result;
	}

	private String loadResultAsConfigured(MockedAdapterSetupEntry setup, String target) {
		String resultFilePath =  setup.getResultFilePathFor(target);
		LOG.info("will use result file path :{} for targeturl:{}",resultFilePath, target);
		if (resultFilePath==null) {
			throw new IllegalStateException("result file path not configured!");
		}

		/* load the result file from disk:*/
		String resource = mockSupport.loadResourceString(resultFilePath);
		return resource;
	}

	private void throwExceptionIfConfigured(C config, MockedAdapterSetupEntry setup, String target)
			throws AdapterException {
		if (setup.isThrowingAdapterExceptionFor(target)) {
			LOG.info("adapter setup wants an error, so start throwing");
			throw asAdapterException("Wanted mock failure for "+target, config);
		}
	}

	private void waitIfConfigured(long timeStarted, MockedAdapterSetupEntry setup, String target) {
		long wantedMs = setup.getTimeToElapseInMilliseconds(target);

		long elapsedMs = System.currentTimeMillis()-timeStarted;
		long timeToWait = wantedMs-elapsedMs;
		if (timeToWait>0) {
			try {
				Thread.sleep(timeToWait);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Check config data is as written in yaml file! This will check that all params
	 * are really given to the mock - means e.g. no data missing or accidently using
	 * defaults.
	 *
	 * @param config
	 */
	protected abstract void validateConfigAsDefinedInMockYAML(C config);

	public String getAdapterId() {
		return createAdapterId();
	}

	@Override
	protected String createAdapterId() {
		/* currently same as in normal adapters but to ensure we got simple names here always - even when changed in adapters - this is overridden*/
		return getClass().getSimpleName();
	}
}
