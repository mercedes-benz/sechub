// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.messaging;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.docgen.reflections.Reflections;
import com.mercedesbenz.sechub.docgen.util.ReflectionsFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsRecevingSyncMessages;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessages;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswers;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

class MessagingAnnotationsCorrectDefinedTest {

    private static TestContext context;

    @BeforeAll
    static void beforeAll() {
        Reflections reflections = ReflectionsFactory.create();

        context = new TestContext();
        context.inspectMessagingCalls(reflections);
    }

    @BeforeEach
    void beforeEach() {
        context.clearErrors();
    }

    @Test
    public void received_async_messages_have_at_least_one_async_sender() throws Exception {
        /* prepare */

        /* test */
        for (MessageID sent : context.messagesReceivedAsynchronous) {
            if (!context.messagesSentAsynchronous.contains(sent)) {
                context.markDiagramGenerationProblem(sent, "received asynchronous, but never sent asynchronous");
            }
        }

        if (context.isNotOkay()) {
            fail(context.toString());
        }
    }

    @Test
    public void sent_async_messages_must_have_at_least_one_async_receiver() throws Exception {
        /* prepare */

        /* test */
        for (MessageID sent : context.messagesSentAsynchronous) {
            if (!context.messagesReceivedAsynchronous.contains(sent)) {
                context.markDiagramGenerationProblem(sent, "sent asynchronous, but never received asynchronous");
            }
        }

        if (context.isNotOkay()) {
            fail(context.toString());
        }
    }

    @Test
    public void received_sync_messages_have_at_least_one_sync_sender() throws Exception {
        /* prepare */

        /* test */
        for (MessageID sent : context.messagesReceivedSynchronous) {
            /* synchron way: */
            // A -> sends synchron message --> can be handled by ONE receiver (not more)
            // B -> receives synchron message and returns at LEAST ONE answer
            if (!context.messagesSentSynchronous.contains(sent)) {
                context.markDiagramGenerationProblem(sent, "received synchronous message but never sent");
            }
        }

        if (context.isNotOkay()) {
            fail(context.toString());
        }
    }

    @Test
    public void sent_sync_messages_have_at_least_one_sync_receiver() throws Exception {
        /* prepare */

        /* test */
        for (MessageID sent : context.messagesSentSynchronous) {
            if (!context.messagesReceivedSynchronous.contains(sent)) {
                context.markDiagramGenerationProblem(sent, "sent synchronous, but never received synchronous");
            }
        }

        if (context.isNotOkay()) {
            fail(context.toString());
        }
    }

    @Test
    public void received_sync_messages_may_not_be_received_asynchronous() throws Exception {
        /* prepare */

        /* test */
        for (MessageID sent : context.messagesReceivedSynchronous) {
            if (context.messagesReceivedAsynchronous.contains(sent)) {
                context.markDiagramGenerationProblem(sent, "received synchronous, but also asynchronous");
            }
        }

        if (context.isNotOkay()) {
            fail(context.toString());
        }
    }

    @Test
    public void received_async_messages_may_not_be_received_synchronous() throws Exception {
        /* prepare */

        /* test */
        for (MessageID sent : context.messagesReceivedAsynchronous) {
            if (context.messagesReceivedSynchronous.contains(sent)) {
                context.markDiagramGenerationProblem(sent, "received asynchronous, but also synchronous");
            }
        }

        if (context.isNotOkay()) {
            fail(context.toString());
        }
    }

    @Test
    public void all_message_ids_are_sent_at_least_one_time() throws Exception {
        /* prepare */
        Set<MessageID> allSentMessages = new HashSet<>();
        allSentMessages.addAll(context.messagesSentAsynchronous);
        allSentMessages.addAll(context.messagesSentSynchronous);

        /* test */
        for (MessageID message : MessageID.values()) {
            if (!allSentMessages.contains(message)) {
                if (context.messagesAnswerResultSynchronous.contains(message)) {
                    /* ignore - it is used as a result for synchronous call, so "sent" and okay */
                    continue;
                }
                context.markDiagramGenerationProblem(message, "is defined, but never sent");
            }
        }

        if (context.isNotOkay()) {
            fail(context.toString());
        }
    }

    @Test
    public void all_message_ids_are_received_at_least_one_time() throws Exception {
        /* prepare */
        Set<MessageID> allSentMessages = new HashSet<>();
        allSentMessages.addAll(context.messagesReceivedAsynchronous);
        allSentMessages.addAll(context.messagesReceivedSynchronous);

        Set<MessageID> notReceivedAsynchronousMessages = new HashSet<>();
        for (MessageID message : MessageID.values()) {
            if (!allSentMessages.contains(message)) {
                notReceivedAsynchronousMessages.add(message);
            }
        }

        /* test */
        for (MessageID message : notReceivedAsynchronousMessages) {
            // when also not received synchronous this is a problem:
            if (!context.messagesAnswerResultSynchronous.contains(message)) {
                context.markDiagramGenerationProblem(message, "is defined, but never received");
            }
        }

        if (context.isNotOkay()) {
            fail(context.toString());
        }
    }

    private static class TestContext {

        StringBuilder sb = new StringBuilder();

        private Set<MessageID> messagesSentAsynchronous;
        private Set<MessageID> messagesSentSynchronous;

        private Set<MessageID> messagesReceivedAsynchronous;
        private Set<MessageID> messagesReceivedSynchronous;

        private Set<MessageID> messagesAnswerResultSynchronous;
        private Set<MessageID> messagesAnsweringToSynchronous;

        public TestContext() {
            messagesSentAsynchronous = new HashSet<>();
            messagesReceivedAsynchronous = new HashSet<>();

            messagesSentSynchronous = new HashSet<>();
            messagesReceivedSynchronous = new HashSet<>();
            messagesAnswerResultSynchronous = new HashSet<>();
            messagesAnsweringToSynchronous = new HashSet<>();

            ;
        }

        public void markDiagramGenerationProblem(MessageID message, String problem) {
            sb.append("\nGenerated diagram problem:Message ");
            sb.append(message.name());
            sb.append(":");
            sb.append(problem);
        }

        public void clearErrors() {
            sb = new StringBuilder();
        }

        private void inspectMessagingCalls(Reflections reflections) {
            /* check methods only annotated directly */
            addInspectionDataFor(IsRecevingSyncMessage.class, context.messagesReceivedSynchronous, reflections);
            addInspectionDataFor(IsSendingSyncMessage.class, context.messagesSentSynchronous, reflections);

            addInspectionDataFor(IsReceivingAsyncMessage.class, context.messagesReceivedAsynchronous, reflections);
            addInspectionDataFor(IsSendingAsyncMessage.class, context.messagesSentAsynchronous, reflections);

            addInspectionDataFor(IsSendingSyncMessageAnswer.class, context.messagesAnswerResultSynchronous, reflections);
            addInspectionDataFor(IsSendingSyncMessageAnswer.class, context.messagesAnsweringToSynchronous, reflections, "answeringTo");

            /* when used multiple annotations we need another appraoch: */
            addInspectionDataFor(context.messagesReceivedSynchronous, reflections,
                    new ContainerAnnotationExtractor<>(IsRecevingSyncMessages.class, "value", IsRecevingSyncMessage.class, "value"));
            // IsSendingSyncMessages does not exist - so not necessary here
            // IsReceivingAsyncMessages does not exist - so not necessary here
            addInspectionDataFor(context.messagesSentAsynchronous, reflections,
                    new ContainerAnnotationExtractor<>(IsSendingAsyncMessages.class, "value", IsSendingAsyncMessage.class, "value"));

            addInspectionDataFor(context.messagesAnswerResultSynchronous, reflections,
                    new ContainerAnnotationExtractor<>(IsSendingSyncMessageAnswers.class, "value", IsSendingSyncMessageAnswer.class, "value"));
            addInspectionDataFor(context.messagesAnsweringToSynchronous, reflections,
                    new ContainerAnnotationExtractor<>(IsSendingSyncMessageAnswers.class, "value", IsSendingSyncMessageAnswer.class, "answeringTo"));

        }

        private <A extends Annotation> void addInspectionDataFor(Class<A> annotationClass, Set<MessageID> set, Reflections reflections) {
            addInspectionDataFor(set, reflections, new AnnotationExtractor<>(annotationClass, "value"));
        }

        private <A extends Annotation> void addInspectionDataFor(Class<A> annotationClass, Set<MessageID> set, Reflections reflections,
                String annotationValue) {
            addInspectionDataFor(set, reflections, new AnnotationExtractor<>(annotationClass, "value"));
        }

        private <A extends Annotation> void addInspectionDataFor(Set<MessageID> set, Reflections reflections, AnnotationExtractor<A> extractor) {

            Set<Method> methodsFoundAnnotatedWithThis = reflections.getMethodsAnnotatedWith(extractor.annotationClass);
            for (Method method : methodsFoundAnnotatedWithThis) {
                A[] annotations = method.getAnnotationsByType(extractor.annotationClass);
                extractor.doExtract(set, annotations);

            }
        }

        public class AnnotationExtractor<A extends Annotation> {
            protected String annotationAttribute;
            protected Class<A> annotationClass;

            public AnnotationExtractor(Class<A> annotationClass, String annotationAttribute) {
                this.annotationClass = annotationClass;
                this.annotationAttribute = annotationAttribute;
            }

            protected void doExtract(Set<MessageID> set, A[] annotations) {
                for (A annotation : annotations) {
                    Object messageID;
                    try {
                        messageID = annotationClass.getMethod(annotationAttribute).invoke(annotation);
                    } catch (Exception e) {
                        throw new IllegalStateException("Was not able to inspect: " + annotationClass, e);
                    }
                    addNullSafe(set, (MessageID) messageID);
                }
            }
        }

        public class ContainerAnnotationExtractor<A extends Annotation, C extends Annotation> extends AnnotationExtractor<A> {

            private Class<C> childAnnotationClass;
            private String childAnnotationAttribute;

            public ContainerAnnotationExtractor(Class<A> containerAnnotationClass, String containerAnnotationAttribute, Class<C> childAnnotationClass,
                    String childAnnotationAttribute) {
                super(containerAnnotationClass, containerAnnotationAttribute);
                this.childAnnotationClass = childAnnotationClass;
                this.childAnnotationAttribute = childAnnotationAttribute;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void doExtract(Set<MessageID> set, A[] annotations) {
                AnnotationExtractor<C> childExtractor = new AnnotationExtractor<>(childAnnotationClass, childAnnotationAttribute);
                for (A containerAnnotation : annotations) {
                    Object childAnnotations;
                    try {
                        childAnnotations = annotationClass.getMethod(annotationAttribute).invoke(containerAnnotation);
                    } catch (Exception e) {
                        throw new IllegalStateException("Was not able to inspect: " + annotationClass, e);
                    }
                    childExtractor.doExtract(set, (C[]) childAnnotations);
                }
            }
        }

        private void addNullSafe(Set<MessageID> set, MessageID value) {
            if (value == null) {
                return;
            }
            set.add(value);
        }

        public boolean isNotOkay() {
            return sb.length() > 0;
        }

        @Override
        public String toString() {
            return sb.toString();
        }

    }

}
