// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.util.StacktraceUtil;

/**
 * This class is able to execute actions which shall be resilient by help of
 * consultants. The proposals from consultants will be inspected and used for
 * resilient behaviour. <br>
 * <br>
 * But be aware: You <b>MUST</b> use one executor always for same kind of
 * action. If you have dedicated actions you need different executors!. An
 * example: When you want to connect to 2 different servers and you want a
 * fallthrough if server 1 is not available you do not want to have a
 * fallthrough of server2 automatically. So in this case you should use two
 * different executors for these different targets !
 *
 * @author Albert Tregnaghi
 *
 */
public class ResilientActionExecutor<R> implements ResilientExecutor<ResilientAction<R>, R> {

    private static final Logger LOG = LoggerFactory.getLogger(ResilientActionExecutor.class);

    private List<ResilienceConsultant> consultants;
    private FallthroughSupport fallThroughSupport;

    public ResilientActionExecutor() {
        fallThroughSupport = new FallthroughSupport();
        consultants = new ArrayList<>();
    }

    public R executeResilient(ResilientAction<R> action, ResilienceCallback callback) throws Exception {
        Objects.requireNonNull(action, "action may not be null!");

        fallThroughSupport.handleFallThrough();

        ResilienctActionContext context = new ResilienctActionContext(callback);

        do {
            try {
                return action.execute();

            } catch (Exception e) {
                handleException(context, e);
            }

        } while (context.isRetryNecessary());

        return null;
    }

    private void handleException(ResilienctActionContext context, Exception e) throws Exception, InterruptedException {
        LOG.info("Handle exception of type:{}", e.getClass().getName());

        context.prepareForNextCheck(e);
        ResilienceCallback callback = context.getCallback();

        ResilienceProposal proposal = findFirstProposalFromConsultants(context);
        if (proposal == null) {
            LOG.info("None of the consultants ({}) gave any proposal, so rethrow exception {}", consultants.size(), StacktraceUtil.createDescription(e));
            throw e;
        }
        if (proposal instanceof RetryResilienceProposal) {

            RetryResilienceProposal retryProposal = (RetryResilienceProposal) proposal;

            int maxRetries = retryProposal.getMaximumAmountOfRetries();
            int alreadyDoneRetries = context.getAlreadyDoneRetries();

            if (alreadyDoneRetries >= maxRetries) {
                LOG.warn("Maximum retry amount ({}/{}) reached, will rethrow exception for :{}", alreadyDoneRetries, maxRetries, proposal.getInfo());
                throw e;
            } else {
                context.forceRetry();
                LOG.debug("Wait {} millis before retry: {}", retryProposal.getMillisecondsToWaitBeforeRetry(), proposal.getInfo());

                /* wait time for next retry */
                Thread.sleep(retryProposal.getMillisecondsToWaitBeforeRetry());
                LOG.info("Retry {}/{}:{}", alreadyDoneRetries, maxRetries, proposal.getInfo());

                if (callback != null) {
                    callback.beforeRetry(context);
                }
            }
        } else if (proposal instanceof FallthroughResilienceProposal) {
            FallthroughResilienceProposal fallThroughProposal = (FallthroughResilienceProposal) proposal;
            LOG.info("Fall through activated, will rethrow same exception {} milliseconds", fallThroughProposal.getMillisecondsForFallThrough());

            context.forceFallThrough(fallThroughProposal);
            throw e;
        } else {
            LOG.error("Returned proposal is not wellknown and so cannt be handled:{}", proposal.getClass().getName());
            throw e;
        }
    }

    private ResilienceProposal findFirstProposalFromConsultants(ResilienctActionContext context) {
        ResilienceProposal proposal = null;
        for (ResilienceConsultant consultant : consultants) {
            proposal = consultant.consultFor(context);
            if (proposal != null) {
                break;
            }
        }
        return proposal;
    }

    public void add(ResilienceConsultant consultant) {
        Objects.requireNonNull(consultant, "consultant may not be null!");
        consultants.add(consultant);
    }

    class ResilienctActionContext implements ResilienceContext {

        private Exception currentError;
        private int retriesCount;
        private boolean retryNecessary;
        private ResilienceCallback callback;
        private Map<String, Object> map = new HashMap<>(1);

        public ResilienctActionContext(ResilienceCallback callback) {
            this.callback = callback;
        }

        public void prepareForNextCheck(Exception e) {
            this.retryNecessary = false;
            this.currentError = e;
        }

        public void forceFallThrough(FallthroughResilienceProposal proposal) {
            this.retryNecessary = false;
            fallThroughSupport.enable(currentError, proposal.getInfo(), proposal.getMillisecondsForFallThrough());
        }

        public boolean isRetryNecessary() {
            return retryNecessary;
        }

        public void forceRetry() {
            retryNecessary = true;
            retriesCount++;
        }

        public void setCurrentError(Exception currentError) {
            this.currentError = currentError;
        }

        @Override
        public Exception getCurrentError() {
            return currentError;
        }

        @Override
        public int getAlreadyDoneRetries() {
            return retriesCount;
        }

        /**
         * @return callback or <code>null</code>
         */
        public ResilienceCallback getCallback() {
            return callback;
        }

        @Override
        public <V> void setValue(String key, V value) {
            if (key == null) {
                LOG.warn("Cannot set a value for key null!");
                return;
            }
            map.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public <V> V getValueOrNull(String key) {
            if (key == null) {
                LOG.warn("Cannot get a value for key null!");
                return null;
            }
            Object value = map.get(key);
            if (value == null) {
                return null;
            }

            /* value found - so cast or fail */
            V castedValueOrNull = null;
            try {
                castedValueOrNull = (V) value;
            } catch (ClassCastException e) {
                LOG.error("Was not able to cast key:{} to wanted type. Was orignally: {}", key, value.getClass(), e);
            }
            return castedValueOrNull;
        }

    }

    class FallthroughSupport {
        private static final long NO_FALLTHROUGH = 0;

        private Exception lastError;
        private String infoFallThrough;
        private Object monitorOBject = new Object();
        private long timeFallthroughEnd;

        public void handleFallThrough() throws Exception {
            synchronized (monitorOBject) {
                if (unsafeIsFallThroughActive()) {
                    LOG.info("Fall through active for {} milliseconds:{}", timeFallthroughEnd - System.currentTimeMillis(), infoFallThrough);
                    throw lastError;
                }
            }
        }

        private boolean unsafeIsFallThroughActive() {
            if (lastError == null) {
                return false;
            }
            if (timeFallthroughEnd == NO_FALLTHROUGH) {
                return false;
            }
            long currentTimeMillis = System.currentTimeMillis();
            long millisUntilFallThroughTimeOut = timeFallthroughEnd - currentTimeMillis;
            if (millisUntilFallThroughTimeOut > 0) {
                return true;
            } else {
                /* reset */
                lastError = null;
                timeFallthroughEnd = NO_FALLTHROUGH;
                return false;
            }
        }

        public void enable(Exception currentError, String info, long millisecondsForFallThrough) {
            synchronized (monitorOBject) {
                this.lastError = currentError;
                this.infoFallThrough = info;
                this.timeFallthroughEnd = System.currentTimeMillis() + millisecondsForFallThrough;
            }

        }

    }

    public boolean containsConsultant(Class<? extends ResilienceConsultant> consultantClass) {
        Objects.requireNonNull(consultantClass);
        if (consultants.isEmpty()) {
            return false;
        }
        for (ResilienceConsultant consultant : consultants) {
            if (consultant.getClass().isAssignableFrom(consultantClass)) {
                return true;
            }
        }
        return false;
    }

}
