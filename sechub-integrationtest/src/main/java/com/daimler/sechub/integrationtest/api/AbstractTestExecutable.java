// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

public abstract class AbstractTestExecutable implements TestExecutable{

	private TestUser user;
	private int timeoutInSeconds;
	private boolean success;
	private Class<? extends Exception>[] handledExceptions;
	private long timeToWaitInMillis;
	@SuppressWarnings("unchecked")
	public AbstractTestExecutable(TestUser user, int timeoutInSeconds, Class<? extends Exception> ...handledExceptions) {
		this(user,timeoutInSeconds,300,handledExceptions);
	}
	@SuppressWarnings("unchecked")
	public AbstractTestExecutable(TestUser user, int timeoutInSeconds, long timeToWaitInMillis, Class<? extends Exception> ...handledExceptions) {
		this.user=user;
		this.timeoutInSeconds=timeoutInSeconds;
		this.timeToWaitInMillis=timeToWaitInMillis;

		if (handledExceptions==null) {
			this.handledExceptions=new Class[0];
		}else {
			this.handledExceptions=handledExceptions;
		}
	}
	@Override
	public TestUser getUser() {
		return user;
	}

	@Override
	public long getTimeToWaitInMillis() {
		return timeToWaitInMillis;
	}
	@Override
	public int getTimeoutInSeconds() {
		return timeoutInSeconds;
	}

	@Override
	public Class<? extends Exception>[] getHandledExceptions() {
		return handledExceptions;
	}

	@Override
	public boolean run() throws Exception {
		boolean result = runImpl();
		if (result) {
			success=true;
		}
		return result;
	}



	public abstract boolean runImpl() throws Exception;

	@Override
	public boolean wasSuccessful() {
		return success;
	}
}