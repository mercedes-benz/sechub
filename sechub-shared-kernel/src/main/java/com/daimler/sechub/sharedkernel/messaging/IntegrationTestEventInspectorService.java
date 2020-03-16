package com.daimler.sechub.sharedkernel.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Profiles;

/**
 * Integrationtest variant - will inspect eventhandling for dedicated usecases
 * and can be fetched by special EventTraceIntTest variants to get information
 * about event history for usecases - leads to automated documentation.
 * 
 * @author Albert Tregnaghi
 *
 */
@Service
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestEventInspectorService implements EventInspector {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestEventInspectorService.class);

    private static final IntegrationTestEventHistory EMPTY_HISTORY = new IntegrationTestEventHistory();// but without identifier and empty;

    int inspectionIdCounter = 0;
    private IntegrationTestEventHistory history;

    public void start() {
        resetAndStop();
        this.history = new IntegrationTestEventHistory(); // means restart
    }

    public void resetAndStop() {
        this.inspectionIdCounter = 0;
        this.history = null; // means stop
    }

    public IntegrationTestEventHistory getHistory() {
        if (isStopped()) {
            return EMPTY_HISTORY;
        }
        return history;
    }

    @Override
    public int createInspectionId() {
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

    @Override
    public void inspectSendSynchron(DomainMessage request, int inspectId) {
        if (isStopped()) {
            /* just not history wanted - ignore */
            return;
        }
        /* identify sender (this method = 1, caller = 2... */
        IntegrationTestEventHistoryInspection inspection = history.ensureInspection(inspectId);
        StackTraceElement traceElement = grabTraceElement();
        inspection.setSynchronousSender(extractRealClassNameFromStacktrace(traceElement), request.getMessageId());

    }

    private boolean isStopped() {
        return history == null;
    }

    @Override
    public void inspectSendAsynchron(DomainMessage request, int inspectId) {
        if (isStopped()) {
            /* just not history wanted - ignore */
            return;
        }
        /* identify sender (this method = 1, caller = 2... */
        IntegrationTestEventHistoryInspection inspection = history.ensureInspection(inspectId);

        StackTraceElement traceElement = grabTraceElement();
        inspection.setAsynchronousSender(extractRealClassNameFromStacktrace(traceElement), request.getMessageId());
    }

    private String extractRealClassNameFromStacktrace(StackTraceElement traceElement) {
        /* here we solve a little problem: spring uses proxies and these are at runtime in stacktrace named. But we want 
         * to return the real class behind. Unfortunately we cannot use org.springframework.util.ClassUtils here, because we have
         * not the class nor the instance. But CGLib which is used by spring does provide classnames always with realService%$...
         * so we can just shrink the class name to get the real one.
         */
        String className = traceElement.getClassName();
        int index = className.indexOf("$$");
        if (index==-1) {
            return className;
        }
        /* CGLIb enhanced so return only parts before to get real name*/
        return className.substring(0,index);
    }

    private StackTraceElement grabTraceElement() {
        StackTraceElement traceElement = grabTracElementOrNull(5); // 5 because:
                                                                   // caller->service->inspectormethod->grabTraceElement->grabTracElementOrNull->getStackTrace
        if (traceElement == null) {
            throw new IllegalStateException("Trace element may not be null!");
        }
        return traceElement;
    }

    @Override
    public void inspectReceiveSynchronMessage(DomainMessage request, int inspectId, SynchronMessageHandler handler) {
        if (isStopped()) {
            /* just not history wanted - ignore */
            return;
        }
        /* identify receiver == handler */
        IntegrationTestEventHistoryInspection inspection = history.ensureInspection(inspectId);
        Class<? extends SynchronMessageHandler> handlerClazz = handler.getClass();
        inspection.getReceiverClassNames().add(handlerClazz.getName());

    }

    @Override
    public void inspectReceiveAsynchronMessage(DomainMessage request, int inspectId, AsynchronMessageHandler handler) {
        if (isStopped()) {
            /* just not history wanted - ignore */
            return;
        }

        /* identify receiver == handler */
        IntegrationTestEventHistoryInspection inspection = history.ensureInspection(inspectId);
        Class<? extends AsynchronMessageHandler> handlerClazz = handler.getClass();
        inspection.getReceiverClassNames().add(handlerClazz.getName());

    }

    /**
     * Grabs trace element
     * 
     * @param pos position in stack trace, starts with 0, where 0 is this method!
     * @return trace element for given position or <code>null</code>
     */
    private StackTraceElement grabTracElementOrNull(int pos) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        int c = 0;
        for (StackTraceElement element : elements) {
            if (c == pos) {
                return element;
            }
            c++;
        }
        return null;
    }

}
