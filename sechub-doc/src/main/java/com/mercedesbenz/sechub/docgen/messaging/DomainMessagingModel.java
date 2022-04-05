// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.messaging;

import static com.mercedesbenz.sechub.docgen.GeneratorConstants.*;
import static com.mercedesbenz.sechub.docgen.util.DocReflectionUtil.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.docgen.util.DocReflectionUtil;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsRecevingSyncMessages;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessages;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswers;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

public class DomainMessagingModel {

    private static final Logger LOG = LoggerFactory.getLogger(DomainMessagingModel.class);

    List<Domain> domains = new ArrayList<>();

    class Domain {
        String name;
        List<DomainPart> domainParts = new ArrayList<>();

        public DomainPart ensureDomainPart(String name) {
            for (DomainPart domain : domainParts) {
                if (domain.name.equals(name)) {
                    return domain;
                }
            }
            DomainPart domainPart = new DomainPart();
            domainPart.name = name;
            domainParts.add(domainPart);
            return domainPart;
        }

        @Override
        public String toString() {
            return "Domain:" + name;
        }

        public DomainPart newDomainPart() {
            return new DomainPart();
        }
    }

    class DomainPart {
        String name;

        List<IsSendingAsyncMessage> sendingAsyncMessages = new ArrayList<>();
        List<IsSendingSyncMessage> sendingSyncMessages = new ArrayList<>();
        List<IsSendingSyncMessageAnswer> sendingSyncMessageAnswers = new ArrayList<>();

        List<IsReceivingAsyncMessage> receivingAsyncMessages = new ArrayList<>();
        List<IsRecevingSyncMessage> recevingSyncMessages = new ArrayList<>();

        Set<MessageID> involvedWithMessages = new TreeSet<>();

        @SuppressWarnings("unchecked")
        public <T extends Annotation> void addMessageInfo(T info) {
            if (DEBUG) {
                LOG.info("adding:{}", info);
            }
            Class<? extends Annotation> clazz = info.getClass();
            clazz = resolveUnproxiedClass(clazz);
            List<T> all = new ArrayList<>();
            if (clazz.equals(IsSendingSyncMessageAnswers.class)) {
                IsSendingSyncMessageAnswers answers = (IsSendingSyncMessageAnswers) info;
                for (IsSendingSyncMessageAnswer answer : answers.value()) {
                    markContaining(answer);
                    all.add((T) answer);
                }
            } else if (clazz.equals(IsRecevingSyncMessages.class)) {
                IsRecevingSyncMessages answers = (IsRecevingSyncMessages) info;
                for (IsRecevingSyncMessage answer : answers.value()) {
                    markContaining(answer);
                    all.add((T) answer);
                }
            } else if (clazz.equals(IsSendingAsyncMessages.class)) {
                IsSendingAsyncMessages answers = (IsSendingAsyncMessages) info;
                for (IsSendingAsyncMessage answer : answers.value()) {
                    markContaining(answer);
                    all.add((T) answer);
                }
            } else {
                markContaining(info);
                all.add(info);
            }
            Class<? extends Annotation> targetClazz = all.iterator().next().getClass();
            @SuppressWarnings("rawtypes")
            List list = fetchList(targetClazz);
            if (DEBUG) {
                LOG.info("using list for target clazz:{}", targetClazz);
            }
            /* hack ... because generic warnings . but works... */
            for (T toAdd : all) {
                list.add(toAdd);
            }
            if (DEBUG) {
                LOG.info(">>> list now::{}", list);
            }
        }

        private <T extends Annotation> void markContaining(T info) {
            Class<? extends Annotation> clazz = DocReflectionUtil.resolveUnproxiedClass(info.getClass());
            try {
                Object result = clazz.getMethod("value").invoke(info);
                if (result instanceof MessageID) {
                    MessageID messageId = (MessageID) result;
                    involvedWithMessages.add(messageId);
                } else {
                    throw new IllegalStateException(clazz.getSimpleName() + ".value() must return a MessageId instance!");
                }
            } catch (Exception e) {
                throw new IllegalStateException("must have access to " + clazz.getSimpleName() + ".value()!");
            }
        }

        @SuppressWarnings("unchecked")
        public <T extends Annotation> List<T> fetchList(Class<T> clazz) {
            Class<? extends Annotation> anno = resolveUnproxiedClass(clazz);
            if (anno.isAssignableFrom(IsSendingAsyncMessage.class)) {
                return (List<T>) sendingAsyncMessages;
            } else if (anno.isAssignableFrom(IsSendingSyncMessage.class)) {
                return (List<T>) sendingSyncMessages;
            } else if (anno.isAssignableFrom(IsSendingSyncMessageAnswer.class)) {
                return (List<T>) sendingSyncMessageAnswers;
            } else if (anno.isAssignableFrom(IsReceivingAsyncMessage.class)) {
                return (List<T>) receivingAsyncMessages;
            } else if (anno.isAssignableFrom(IsRecevingSyncMessage.class)) {
                return (List<T>) recevingSyncMessages;
            }
            throw new IllegalArgumentException("Domain message model does not handle:" + anno);
        }

        @Override
        public String toString() {
            return "DomainPart:" + name;
        }

        public boolean isHandlingAtLeastOneOf(MessageID[] messageIDs) {
            for (MessageID id : messageIDs) {
                if (this.involvedWithMessages.contains(id)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Will return normally the value. but for sync answering the reason for the
     * answer will be returned!
     *
     * @param anno
     * @return
     */
    public <T extends Annotation> MessageID getMessageIdToSearchFor(T anno) {
        if (anno instanceof IsSendingAsyncMessage) {
            return ((IsSendingAsyncMessage) anno).value();
        } else if (anno instanceof IsSendingSyncMessage) {
            return ((IsSendingSyncMessage) anno).value();
        } else if (anno instanceof IsSendingSyncMessageAnswer) {
            return ((IsSendingSyncMessageAnswer) anno).answeringTo();
        } else if (anno instanceof IsReceivingAsyncMessage) {
            return ((IsReceivingAsyncMessage) anno).value();
        } else if (anno instanceof IsRecevingSyncMessage) {
            return ((IsRecevingSyncMessage) anno).value();
        }
        throw new IllegalArgumentException("Not handling:" + anno);
    }

    public Domain ensureDomain(String name) {
        for (Domain domain : domains) {
            if (domain.name.equals(name)) {
                return domain;
            }
        }
        Domain domain = new Domain();
        domain.name = name;
        domains.add(domain);
        return domain;
    }

    public <T extends Annotation> List<DomainPartMessageInfo<T>> createDomainPartMessageInfos(Class<T> clazz, MessageID messageId) {
        List<DomainPartMessageInfo<T>> list = new ArrayList<>();
        for (Domain domain : domains) {
            for (DomainPart domainPart : domain.domainParts) {
                updateGivenDomainPartMessageInfoList(clazz, messageId, list, domain, domainPart);
            }
        }
        return list;
    }

    private <T extends Annotation> void updateGivenDomainPartMessageInfoList(Class<T> clazz, MessageID messageId, List<DomainPartMessageInfo<T>> list,
            Domain domain, DomainPart domainPart) {
        List<T> receiverClazzList = domainPart.fetchList(clazz);
        if (DEBUG) {
            LOG.info(" >enter:{},{}, found:{}", domain, domainPart, list);
        }
        for (T receiverClazz : receiverClazzList) {
            MessageID foundMessageId = getMessageIdToSearchFor(receiverClazz);
            boolean sameMessageId = foundMessageId == messageId;
            if (sameMessageId) {
                DomainPartMessageInfo<T> info = new DomainPartMessageInfo<>();
                info.messageInfo = receiverClazz;
                info.part = domainPart;
                if (receiverClazz instanceof IsSendingSyncMessageAnswer) {
                    IsSendingSyncMessageAnswer answer = (IsSendingSyncMessageAnswer) receiverClazz;
                    info.messageId = answer.value();
                    info.answeringTo = answer.answeringTo();
                    info.branchName = answer.branchName();
                } else {
                    info.messageId = messageId;
                }
                list.add(info);
            }
        }
    }

    public class DomainPartMessageInfo<T extends Annotation> {
        DomainPart part;
        T messageInfo;
        MessageID messageId;
        MessageID answeringTo;
        String branchName;
    }

    public Domain newDomain() {
        return new Domain();
    }

}
