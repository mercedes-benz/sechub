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
            StackTraceElement traceElement = grabTracElementWithoutProxies(4);
            inspection.setSynchronousSender(extractRealClassNameFromStacktrace(traceElement), request.getMessageId());
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

            StackTraceElement traceElement = grabTracElementWithoutProxies(4);
            inspection.setAsynchronousSender(extractRealClassNameFromStacktrace(traceElement), request.getMessageId());
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
        synchronized(monitor) {
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
     * Grabs trace element - will ignore spring proxy stuff automatically
     * 
     * @param pos position in stack trace, starts with 0, where 0 is get trace element, and 1 is this method!
     * @return trace element for given position or <code>null</code>
     */
    private StackTraceElement grabTracElementWithoutProxies(int pos) {
        
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        int i=0;
        LOG.trace("Grab tracelements:{}",pos);
        for (StackTraceElement element: elements) {
            LOG.trace("{}: {}",i++,element);
        }

        int c = -1;
        i= 0;
        for (StackTraceElement element : elements) {
            String className = element.getClassName();
    //       Grab tracelements:4
    //       0:java.lang.Thread.getStackTrace(Thread.java:1559)
    //       1:com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventInspectorService.grabTracElement(IntegrationTestEventInspectorService.java:173)
    //       2:com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventInspectorService.inspectSendAsynchron(IntegrationTestEventInspectorService.java:110)
    //       3:com.daimler.sechub.sharedkernel.messaging.DomainMessageService.sendAsynchron(DomainMessageService.java:153)
    //       4:com.daimler.sechub.sharedkernel.messaging.DomainMessageService$$FastClassBySpringCGLIB$$c7de0c21.invoke(<generated>)
    //       5:org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
    //       6:org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:685)
    //       7:com.daimler.sechub.sharedkernel.messaging.DomainMessageService$$EnhancerBySpringCGLIB$$83df6b4.sendAsynchron(<generated>)
    //       8:com.daimler.sechub.domain.schedule.config.SchedulerConfigService.setJobProcessingEnabled(SchedulerConfigService.java:70)
    //       9:com.daimler.sechub.domain.schedule.config.SchedulerConfigService.enableJobProcessing(SchedulerConfigService.java:38)
            i++;
            boolean proxied = false;
            proxied = proxied || className.indexOf("EnhancerBySpringCGLIB")!=-1;
            proxied = proxied || className.indexOf("org.springframework.cglib")!=-1;
            proxied = proxied || className.indexOf("org.springframework.aop")!=-1;
            if (proxied){
                LOG.trace("Skip proxy stuff {}:{}",i,className);
                continue;
            }
            if (c == pos) {
                LOG.trace("return: {}:{}",i,element);
                return element;
            }
            c++;
        }
        throw new IllegalStateException("Trace element may not be null!");
    }

}
