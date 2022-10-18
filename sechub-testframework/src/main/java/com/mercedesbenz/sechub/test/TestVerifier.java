package com.mercedesbenz.sechub.test;

import java.util.ArrayList;
import java.util.List;

public class TestVerifier {

    private List<TestVerifierCallback> callbacks = new ArrayList<>();

    public void add(TestVerifierCallback callback) {
        callbacks.add(callback);
    }

    /**
     * Calls all test verifier callbacks - e.g. wiremock verification callbacks etc.
     */
    public void verify() {
        for (TestVerifierCallback callback : callbacks) {
            callback.verifyCallback();
        }
    }
}
