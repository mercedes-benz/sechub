// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Endpoint which receives a synchronous message. The endpoint wil handle the message and give dedicated {@link IsSendingSyncMessageAnswer}s
 * @author Albert Tregnaghi
 *
 */
@DomainMessaging
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(IsRecevingSyncMessages.class)
public @interface IsRecevingSyncMessage {

	MessageID value();
	
}
