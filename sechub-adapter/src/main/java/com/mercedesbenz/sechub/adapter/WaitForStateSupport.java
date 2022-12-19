// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Abstract class to support waiting for given states
 *
 * @author Albert Tregnaghi
 *
 * @param <X> context
 * @param <C> configuration
 */
public abstract class WaitForStateSupport<X extends AdapterContext<C>, C extends AdapterConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(WaitForStateSupport.class);
    protected Adapter<?> adapter;

    public WaitForStateSupport(Adapter<?> adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("adapter may not be null!");
        }
        this.adapter = adapter;
    }

    protected abstract boolean isWaitingForOKWhenInState(String state, X context) throws /* NOSONAR */Exception;

    /**
     * Handle state when no longer waiting
     *
     * @param state
     * @param context
     * @throws Exception
     */
    protected void handleNoLongerWaitingState(String state, X context) throws /* NOSONAR */ Exception {
        /* can be overriden, per default do nothing */
    }

    /**
     * Iterative fetching of current state
     *
     * @param context
     * @return
     * @throws Exception
     */
    protected abstract String getCurrentState(X context) throws Exception;

    /**
     * Waits for OK state or another wellknown state. Does also support time out
     * handling depending on context
     *
     * @param context
     * @throws Exception
     */
    public final void waitForOK(X context) throws AdapterException {
        AdapterConfig config = context.getConfig();
        LOG.debug("{} wait for OK", adapter.getAdapterLogId(config));
        String state = null;
        try {
            while (isWaitingForOKWhenInState(state = getCurrentState(context), context)) {
                if (context.isTimeOut()) {
                    throw adapter.asAdapterException("Time out reached:" + context.getMillisecondsRun() + " millis run.", config);
                }
                waitForResult(context);
            }
            handleNoLongerWaitingState(state, context);

        } catch (HttpClientErrorException e) {
            throw adapter.asAdapterException("Waiting for result failed - response body was: " + e.getResponseBodyAsString(), e, config);
        } catch (Exception e) {
            throw adapter.asAdapterException("Waiting for result failed", e, config);
        }
    }

    private void waitForResult(X context) throws AdapterException {
        AdapterConfig config = context.getConfig();
        if (config == null) {
            throw new IllegalStateException("config is null!");
        }
        try {
            LOG.trace("{}  wait for result-STARTED", adapter.getAdapterLogId(config));
            Thread.sleep(config.getTimeToWaitForNextCheckOperationInMilliseconds());
            LOG.trace("{}  wait for result-DONE", adapter.getAdapterLogId(config));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw adapter.asAdapterException("Waiting was interrupted", e, config);
        }

    }

}
