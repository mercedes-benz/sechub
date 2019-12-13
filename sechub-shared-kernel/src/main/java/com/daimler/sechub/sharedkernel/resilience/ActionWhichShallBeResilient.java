package com.daimler.sechub.sharedkernel.resilience;

public interface ActionWhichShallBeResilient<R> {

	/**
	 * Action method which shall be executed in resilient way
	 * @return
	 * @throws Exception
	 */
	public R execute() throws Exception;
}
