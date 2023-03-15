// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;

/**
 * Sereco annotation - represents additional data fetched by importers
 *
 * @author Albert Tregnaghi
 *
 */
public class SerecoAnnotation {

    private static final SerecoAnnotationType FALLBACK_ANNOTATION_TYPE = SerecoAnnotationType.USER_INFO;

    private static final SecHubMessageType FALLBACK_USER_MESSAGE_TYPE = SecHubMessageType.INFO;

    private static final Logger LOG = LoggerFactory.getLogger(SerecoAnnotation.class);

    private SerecoAnnotationType type;

    private String value;

    public static SerecoAnnotation fromSecHubMessage(SecHubMessage message) {
        SerecoAnnotation annotation = new SerecoAnnotation();
        SecHubMessageType sechubMessageType = message.getType();
        if (sechubMessageType == null) {
            sechubMessageType = FALLBACK_USER_MESSAGE_TYPE;
            LOG.warn("No sechub message type defined - fallback to {}", sechubMessageType);
        }
        SerecoAnnotationType annotationType = null;
        switch (sechubMessageType) {
        case ERROR:
            annotationType = SerecoAnnotationType.USER_ERROR;
            break;
        case INFO:
            annotationType = SerecoAnnotationType.USER_INFO;
            break;
        case WARNING:
            annotationType = SerecoAnnotationType.USER_WARNING;
            break;
        default:
            annotationType = FALLBACK_ANNOTATION_TYPE;
            LOG.warn("Sechub message type {} not handled - fallback to {}", sechubMessageType, annotationType);
            break;

        }
        annotation.setType(annotationType);
        annotation.setValue(message.getText());
        return annotation;
    }

    public void setType(SerecoAnnotationType type) {
        this.type = type;
    }

    public SerecoAnnotationType getType() {
        return type;
    }

    public void setValue(String text) {
        this.value = text;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SerecoAnnotation other = (SerecoAnnotation) obj;
        return Objects.equals(value, other.value) && type == other.type;
    }
}
