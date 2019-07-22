// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Endpoint which sends a synchronous answer
 * @author Albert Tregnaghi
 *
 */
@DomainMessaging
@Repeatable(IsSendingSyncMessageAnswers.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IsSendingSyncMessageAnswer {

	/**
	 * Define the message for which is answered here
	 * @return
	 */
	MessageID answeringTo();
	
	/**
	 * Answer type
	 * @return
	 */
	MessageID value();

	/**
	 * Name of branch - e.g. "success" or "failure". Used  in documentation
	 * @return
	 */
	String branchName();

}
