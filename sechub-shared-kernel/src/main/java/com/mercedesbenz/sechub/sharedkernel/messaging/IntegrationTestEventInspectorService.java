// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.util.Map;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.sharedkernel.Profiles;

/**
 * Integration test variant - will inspect event handling for dedicated use
 * cases and can be fetched by special EventTraceIntTest variants to get
 * information about event history for use cases - leads to automated
 * documentation.
 *
 * @author Albert Tregnaghi
 *
 */
@Service
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestEventInspectorService implements EventInspector {

    static final Logger LOG = LoggerFactory.getLogger(IntegrationTestEventInspectorService.class);

    private static final IntegrationTestEventHistory EMPTY_HISTORY = new IntegrationTestEventHistory();// but without identifier and empty;

    int inspectionIdCounter = 0;
    private IntegrationTestEventHistory history;

    private boolean started;
    private static Object monitor = new Object();

    public void start() {
        synchronized (monitor) {
            resetAndStop();
            this.history = new IntegrationTestEventHistory(); // means restart
            started = true;
        }
    }

    public void resetAndStop() {
        synchronized (monitor) {
            started = false;

            this.inspectionIdCounter = 0;
            this.history = null;
        }
    }

    public IntegrationTestEventHistory getHistory() {
        synchronized (monitor) {
            if (isStopped()) {
                return EMPTY_HISTORY;
            }
            return history;
        }
    }

    @Override
    public int createInspectionId() {
        synchronized (monitor) {
            int id = inspectionIdCounter;
            /*
             * we simple increment the id counter, so first inspection is 1, next is 2 etc.
             * its just to identify the group (means sender, and recipients in ONE send) and
             * nothing more
             */
            inspectionIdCounter++;
            LOG.debug("inspection id:{}", id);
            return id;
        }
    }

    public int getInspectionIdCounter() {
        return inspectionIdCounter;
    }

    @Override
    public void inspectSendSynchron(DomainMessage request, int inspectId) {
        synchronized (monitor) {
            if (isStopped()) {
                /* just not history wanted - ignore */
                return;
            }
            /* identify sender (this method = 1, caller = 2... */
            IntegrationTestEventHistoryInspection inspection = history.ensureInspection(inspectId);

            StackTraceElement traceElement = grabFirstNonProxiedDomainTraceElement();
            inspection.setSynchronousSender(extractRealClassNameFromStacktrace(traceElement), request.getMessageId());
            appendAdditionalDebugData(request, inspection);
        }

    }

    private void appendAdditionalDebugData(DomainMessage request, IntegrationTestEventHistoryInspection inspection) {
        IntegrationTestEventHistoryDebugData debug = inspection.getDebug();
        debug.setSenderThread(Thread.currentThread().getName());

        Map<String, String> messageData = debug.getMessageData();
        if (messageData != null && request.parameters != null) {
            /*
             * for integration testing we break the sealed object data, because we need this
             * for event trace tests
             */
            for (Map.Entry<String, SealedObject> entry : request.parameters.entrySet()) {
                String key = entry.getKey();
                SealedObject val = entry.getValue();
                messageData.put(key, CryptoAccess.CRYPTO_STRING.unseal(val));
            }
        }
    }

    private boolean isStopped() {
        return !isStarted();
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public void inspectSendAsynchron(DomainMessage request, int inspectId) {
        synchronized (monitor) {
            if (isStopped()) {
                /* just not history wanted - ignore */
                return;
            }
            /* identify sender (this method = 1, caller = 2... */
            IntegrationTestEventHistoryInspection inspection = history.ensureInspection(inspectId);

            StackTraceElement traceElement = grabFirstNonProxiedDomainTraceElement();
            inspection.setAsynchronousSender(extractRealClassNameFromStacktrace(traceElement), request.getMessageId());
            appendAdditionalDebugData(request, inspection);
        }
    }

    private String extractRealClassNameFromStacktrace(StackTraceElement traceElement) {
        String className = traceElement.getClassName();
        return className;
    }

    @Override
    public void inspectReceiveSynchronMessage(DomainMessage request, int inspectId, SynchronMessageHandler handler) {
        synchronized (monitor) {

            if (isStopped()) {
                /* just not history wanted - ignore */
                return;
            }
            /* identify receiver == handler */
            IntegrationTestEventHistoryInspection inspection = history.ensureInspection(inspectId);
            Class<? extends SynchronMessageHandler> handlerClazz = handler.getClass();
            inspection.getReceiverClassNames().add(handlerClazz.getName());
        }

    }

    @Override
    public void inspectReceiveAsynchronMessage(DomainMessage request, int inspectId, AsynchronMessageHandler handler) {
        synchronized (monitor) {
            if (isStopped()) {
                /* just not history wanted - ignore */
                return;
            }

            /* identify receiver == handler */
            IntegrationTestEventHistoryInspection inspection = history.ensureInspection(inspectId);
            Class<? extends AsynchronMessageHandler> handlerClazz = handler.getClass();
            inspection.getReceiverClassNames().add(handlerClazz.getName());
        }

    }

    /**
     * Grabs first trace element with domain package- will ignore spring proxy stuff
     * automatically
     *
     * @return trace element or <code>null</code>
     */
    private StackTraceElement grabFirstNonProxiedDomainTraceElement() {

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        int elementIndex = 0;
        if (LOG.isTraceEnabled()) {
            for (StackTraceElement element : elements) {
                LOG.trace("{}: {}", elementIndex++, element);
            }
        }
        elementIndex = 0;
        for (StackTraceElement element : elements) {
            String className = element.getClassName();
            /* @formatter:off */
            //  Grab trace elements: new example (Spring Boot3):
            //  -----------------------------------------------
            //  0:java.base/java.lang.Thread.getStackTrace(Thread.java:1619),
            //  1:com.mercedesbenz.sechub.sharedkernel.messaging.IntegrationTestEventInspectorService.grabTracElementWithoutProxies(IntegrationTestEventInspectorService.java:177),
            //  2:com.mercedesbenz.sechub.sharedkernel.messaging.IntegrationTestEventInspectorService.inspectSendAsynchron(IntegrationTestEventInspectorService.java:125),
            //  3:com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService.sendAsynchron(DomainMessageService.java:160), java.base/
            //  4:jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method), java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77), java.base/
            //  5:jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43), java.base/java.lang.reflect.Method.invoke(Method.java:568),
            //  6:org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:351),
            //  7:org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:713), com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService$
            //  8:$SpringCGLIB$$0.sendAsynchron(<generated>), com.mercedesbenz.sechub.domain.administration.config.AdministrationConfigService.sendEvent(AdministrationConfigService.java:109),
            //  9:com.mercedesbenz.sechub.domain.administration.config.AdministrationConfigService.updateAutoCleanupConfiguration(AdministrationConfigService.java:72),
            /* @formatter:on */

            elementIndex++;

            /* check if proxied */
            boolean proxied = false;
            proxied = proxied || className.indexOf("SpringCGLIB") != -1;
            proxied = proxied || className.indexOf("org.springframework.cglib") != -1;
            proxied = proxied || className.indexOf("org.springframework.aop") != -1;
            if (proxied) {
                LOG.trace("Skip proxy stuff {}:{}", elementIndex, className);
                continue;
            }
            if (className.indexOf("sechub.domain.") != -1) {
                LOG.trace("return: {}:{}", elementIndex, element);
                return element;
            }
        }
        throw new IllegalStateException("Trace element may not be null!");
    }

}
