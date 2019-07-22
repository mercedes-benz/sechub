// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@DomainMessaging
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IsReceivingAsyncMessage {

	MessageID value();

}
