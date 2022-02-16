// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SimpleMailMessageSupport {

    /**
     * Describes the message in a short form - will only contain subject, followed
     * by existing mail targets, and existing from and replyTo. But NEVER subject -
     * because could contain sensitive data which should not be logged.
     *
     * @param message
     * @return short message describing topic
     */
    public String describeTopic(SimpleMailMessage message) {
        StringBuilder sb = new StringBuilder();
        sb.append("subject=").append(message.getSubject()).append("; ");

        appendTargets(sb, message.getTo(), "to");
        appendTargets(sb, message.getCc(), "cc");
        appendTargets(sb, message.getBcc(), "bcc");

        appendTarget(sb, message.getFrom(), "from");
        appendTarget(sb, message.getReplyTo(), "replyTo");

        return sb.toString().trim();
    }

    private void appendTarget(StringBuilder sb, String target, String targetType) {
        if (target == null) {
            return;
        }
        appendTargets(sb, new String[] { target }, targetType);
    }

    private void appendTargets(StringBuilder sb, String[] target, String targetType) {
        if (target != null && target.length > 0) {
            sb.append(targetType).append("=").append(StringUtils.arrayToCommaDelimitedString(target)).append("; ");
        }
    }
}
