// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import java.util.Iterator;
import java.util.List;

import com.daimler.sechub.adapter.LoginScriptGenerator;
import com.daimler.sechub.adapter.LoginScriptAction;
import com.daimler.sechub.adapter.SecHubTimeUnit;

public class NetsparkerLoginScriptGenerator implements LoginScriptGenerator {

    public String generate(List<LoginScriptAction> steps) {
        StringBuilder sb = new StringBuilder();

        generate(steps, sb);

        return sb.toString();
    }

    private void generate(List<LoginScriptAction> steps, StringBuilder sb) {
        if (steps == null) {
            return;
        }

        LoginScriptAction previousStep = null;
        LoginScriptAction currentStep = null;
        LoginScriptAction nextStep = null;

        if (steps.size() > 0) {

            Iterator<LoginScriptAction> iter = steps.iterator();

            // the loop needs to iterate one more time, than the element size
            for (int iterations = -1; iterations < (steps.size()); iterations++) {

                previousStep = currentStep;
                currentStep = nextStep;

                // in the last iteration the next step has to become null
                if (iterations < (steps.size() - 1)) {
                    nextStep = iter.next();
                } else {
                    nextStep = null;
                }

                generateStep(previousStep, currentStep, nextStep, sb);
            }
        }

    }

    private void generateStep(LoginScriptAction previousStep, LoginScriptAction currentStep, LoginScriptAction nextStep, StringBuilder sb) {
        if (currentStep == null) {
            return;
        }

        String end = null;
        Long wait = null;

        // Add the description as comment
        String description = currentStep.getDescription();
        if (description != null && !description.isEmpty()) {
            sb.append(generateStepString("/* ", description, " */\n"));
        }

        // check whether it is necessary to add a delay (wait) for the current action
        if (previousStep != null) {
            if (previousStep.isWait() && !currentStep.isWait()) {
                wait = getWaitTimeInMilliseconds(previousStep);
            }
        }

        if (wait != null) {
            // add a delay to the current command
            end = "," + wait + ");\n";
        } else {
            end = ");\n";
        }

        switch (currentStep.getActionType()) {
        case CLICK:
            sb.append(generateStepString("netsparker.auth.clickByQuery('", currentStep.getSelector(), "'", end));
            break;
        case INPUT:
            sb.append(generateStepString("netsparker.auth.setValueByQuery('", currentStep.getSelector(), "','", currentStep.getValue(), "'", end));
            break;
        case PASSWORD:
            sb.append(generateStepString("netsparker.auth.setValueByQuery('", currentStep.getSelector(), "',password", end));
            break;
        case USERNAME:
            sb.append(generateStepString("netsparker.auth.setValueByQuery('", currentStep.getSelector(), "',username", end));
            break;
        case WAIT:
            addTimeoutCommand(currentStep, nextStep, sb);
            break;
        }
    }

    private void addTimeoutCommand(LoginScriptAction currentStep, LoginScriptAction nextStep, StringBuilder sb) {
        if (nextStep == null || nextStep.isWait()) {
            Long timeout = getWaitTimeInMilliseconds(currentStep);

            if (timeout != null) {
                sb.append(generateStepString("setTimeout(function() {},", timeout.toString(), ");\n"));
            }
        }
    }

    private String generateStepString(String... parts) {
        StringBuilder sb = new StringBuilder();

        for (String part : parts) {
            sb.append(part);
        }

        return sb.toString();
    }

    private Long getWaitTimeInMilliseconds(LoginScriptAction step) {
        Long wait = null;

        if (step.isWait()) {
            long waitValue = Long.valueOf(step.getValue());
            SecHubTimeUnit unit = step.getUnit();

            wait = waitValue * unit.getMultiplicatorMilliseconds();
        }

        return wait;
    }
}
