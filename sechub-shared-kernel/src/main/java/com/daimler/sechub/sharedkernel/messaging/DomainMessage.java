// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

/**
 * A domain request represents a domain event which is triggered
 * @author Albert Tregnaghi
 *
 */
public class DomainMessage extends DomainMessagePart {


	public DomainMessage(MessageID id) {
		super(id);
	}


}
