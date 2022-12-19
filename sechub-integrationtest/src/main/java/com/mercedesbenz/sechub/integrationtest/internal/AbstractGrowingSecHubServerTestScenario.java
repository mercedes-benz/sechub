// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractGrowingSecHubServerTestScenario extends AbstractSecHubServerTestScenario implements GrowingScenario {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractGrowingSecHubServerTestScenario.class);

    private PersistentScenarioTestDataProvider scenarioTestDataProvider = new PersistentScenarioTestDataProvider(this);
    private String generatedPrefixMainId;
    private static List<String> generatedPrefixMainIds = new ArrayList<>();

    @Override
    protected void waitForTestDataAvailable() {
        /* we do NEVER wait */

    }

    @Override
    public final void grow() {
        scenarioTestDataProvider.increaseGrowId();
    }

    @Override
    public final String getGrowId() {
        return scenarioTestDataProvider.getGrowId();
    }

    @Override
    public final String getPrefixMainId() {
        if (generatedPrefixMainId == null) {
            generatePrefixMainId();
        }
        return generatedPrefixMainId;
    }

    private void generatePrefixMainId() {
        String className = getClass().getSimpleName();
        char firstChar = className.charAt(0);
        String numberAsString = className.substring(className.length() - 2);

        if (!Character.isDigit(numberAsString.charAt(0))) {
            numberAsString = numberAsString.substring(1);
        }
        try {
            String id = ("" + firstChar).toLowerCase();
            int number = Integer.decode(numberAsString);
            if (number < 10) {
                id = id + 0;
            }
            id = id + number;

            /* check uniqueness of id inside JVM */
            if (generatedPrefixMainIds.contains(id)) {
                throw new IllegalStateException("The generated prefix main id:" + id + " is already generated before:" + generatedPrefixMainIds);
            }
            /* register */
            generatedPrefixMainIds.add(id);
            generatedPrefixMainId = id;

        } catch (NumberFormatException e) {
            throw new IllegalStateException("Test scenarios class names must end with a number! But found class name:" + className);
        }

        LOG.debug("Prefix main id calculated:", generatedPrefixMainId);
    }

}
