// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.resilience;

public interface ResilienceContext {

	public Exception getCurrentError();

	public int getAlreadyDoneRetries();
}
