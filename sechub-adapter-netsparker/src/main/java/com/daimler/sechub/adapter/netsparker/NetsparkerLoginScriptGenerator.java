// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import java.util.Iterator;
import java.util.List;

import com.daimler.sechub.adapter.LoginScriptGenerator;
import com.daimler.sechub.adapter.LoginScriptAction;
import com.daimler.sechub.adapter.SecHubTimeUnit;

public class NetsparkerLoginScriptGenerator implements LoginScriptGenerator{

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
                
                generateScript(previousStep, currentStep, nextStep, sb);
            }
		} 		

	}

    private void generateScript(LoginScriptAction previousStep, LoginScriptAction currentStep, LoginScriptAction nextStep, StringBuilder sb) {
        if (currentStep == null) {
            return;
        }
        
        // Add the description as comment
        String description = currentStep.getDescription();
        if (description != null && !description.isEmpty()) {
            sb.append("// ").append(description);
        }
        
        boolean scriptCommand = true;
        Long wait = null;       
        
        if (previousStep != null) {
            if (previousStep.isWait() && !currentStep.isWait()) {
                wait = getWaitTimeInMilliseconds(previousStep);
            }
        }

        if (currentStep.isClick()) {
            sb.append("netsparker.auth.clickByQuery('").append(currentStep.getSelector()).append("'");
        } else if (currentStep.isInput()) {
            sb.append("netsparker.auth.setValueByQuery('").append(currentStep.getSelector()).append("','").append(currentStep.getValue()).append("'");
        } else if (currentStep.isUserName()) {
            sb.append("netsparker.auth.setValueByQuery('").append(currentStep.getSelector()).append("',username");
        } else if (currentStep.isPassword()) {
            sb.append("netsparker.auth.setValueByQuery('").append(currentStep.getSelector()).append("',password");
        } else if (currentStep.isWait()) {
            if (nextStep == null || nextStep.isWait()) {
              Long timeout = getWaitTimeInMilliseconds(currentStep);
              
              if (timeout != null) {
                  sb.append("setTimeout(function() {},").append(timeout);
              }
            } else {
                scriptCommand = false;
            }
        } else {
            scriptCommand = false;
        }
        
        if (scriptCommand) {           
            if (wait != null) {
                sb.append(",").append(wait).append(");");
            } else {
                sb.append(");");
            }
        }
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
