package com.mercedesbenz.sechub.webui.sechubaccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.SecHubClient;

/**
 * Provides a fluent api, automated logging and error handling. Also hides
 * client object and client exceptions.
 * 
 * @author Albert Tregnaghi
 *
 * @param <T>
 */
public class SecHubClientExecutor<T> {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubClientExecutor.class);

    private SecHubClient client;
    protected String info;
    protected ClientCaller<T> clientCaller;
    protected ErrorCallback<T> errorCallback;
    protected Class<T> resultClazz;
    
    private T fallbackResult;
    private boolean onErrorReturnFallback;

    SecHubClientExecutor(SecHubClient client, Class<T> resultClazz) {
        this.client = client;
        this.resultClazz = resultClazz;
    }

    public SecHubClientExecutor<T> whenDoing(String info) {
        this.info = info;
        return this;
    }

    public SecHubClientExecutor<T> callAndReturn(ClientCaller<T> clientCaller) {
        this.clientCaller = clientCaller;
        return this;
    }

    /**
     * Within this method we can handle errors explicit via an error callback. 
     * 
     * @param errorCallback
     * @return the object to return in case of an error
     */
    public SecHubClientExecutor<T> onErrorReturn(ErrorCallback<T> errorCallback) {
        this.onErrorReturnFallback = false;
        this.errorCallback = errorCallback;
        return this;
    }

    public SecHubClientExecutor<T> onErrorReturnAlways(T fallbackResult) {
        this.onErrorReturnFallback = true;
        this.fallbackResult = fallbackResult;
        return this;
    }

    public T execute() {
        try {
            if (clientCaller == null) {
                throw new IllegalArgumentException("You did not define a client caller - please use call(..) method to define one!");
            }
            return clientCaller.callAndReturn(client);
        } catch (Exception e) {

            LOG.error("Client call failed: {}", info, e);

            if (onErrorReturnFallback) {
                return fallbackResult;
            }

            if (errorCallback == null) {
                if (!resultClazz.isAssignableFrom(Void.class)) {
                    SecHubAccessService.LOG.warn(
                            "Error callback not set for '{}' - will return null for expected result of '{}' result! This is a bug in usage - please use onError(..) method or call allowNullResults() to define correct error result!",
                            info, resultClazz);
                }
                return null;
            }
            return errorCallback.handleExceptionAndReturnFallback(e);
        }
    }

}