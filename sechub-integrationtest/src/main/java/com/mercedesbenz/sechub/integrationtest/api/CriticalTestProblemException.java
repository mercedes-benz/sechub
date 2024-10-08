package com.mercedesbenz.sechub.integrationtest.api;

/**
 * Special exception which shall only be thrown when the tests are in a state
 * where it is no longer possible to execute them correctly.
 *
 * For example: Via TestAPI it is possible to wait until no longer jobs are
 * running. If this is not possible/time out is done, this is a critical
 * behavior because the checks are very time consuming and after the timeout
 * there are still jobs running. But the wait mechanism is used for every
 * integration test and would fail of them but after an extreme long time
 * period.
 *
 * @author Albert Tregnaghi
 *
 */
public class CriticalTestProblemException extends IllegalStateException {

    public CriticalTestProblemException(String s) {
        super(s);
    }

    public CriticalTestProblemException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 1L;

}
