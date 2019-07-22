// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DomainMessageServiceTest {

	@Test
	public void test() {
		List<AsynchronMessageHandler> injectedAsynchronousHandlers = new ArrayList<>();
		List<SynchronMessageHandler> injectedSynchronousHandlers = new ArrayList<>();
		
		DomainMessageService service = new DomainMessageService(injectedSynchronousHandlers, injectedAsynchronousHandlers );
		DomainMessage request = new DomainMessage(MessageID.USER_REMOVED_FROM_PROJECT);
		service.sendAsynchron(request);
	
	}

}
