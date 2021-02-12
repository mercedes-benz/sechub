package com.daimler.sechub.adapter;

public enum LoginScriptStepAction {
    INPUT,
    
    CLICK,
    
    PASSWORD,
    
    USERNAME,
    
    WAIT;
    
    public static LoginScriptStepAction valueOfIgnoreCase(String action) {
        LoginScriptStepAction loginAction = null;

        for (LoginScriptStepAction enumAction : LoginScriptStepAction.values()) {
            if (enumAction.name().equalsIgnoreCase(action)) {
                loginAction = enumAction;
            }
        }
        return loginAction;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
