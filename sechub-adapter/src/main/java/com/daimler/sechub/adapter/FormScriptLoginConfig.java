// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.util.LinkedList;
import java.util.List;

public class FormScriptLoginConfig extends AbstractLoginConfig{

    private List<LoginScriptPage> pages = new LinkedList<>();
    
	public List<LoginScriptPage> getPages() {
        return pages;
    }

	public String getUserName() {
	    for (LoginScriptPage page : pages) {
    		for (LoginScriptAction action : page.getActions()) {
    			if (action.isUserName()) {
    				return action.getValue();
    			}
    		}
	    }
	    
	    return "<unknown-user>";
	}

	public String getPassword() {
	    for (LoginScriptPage page : pages) {
    		for (LoginScriptAction action : page.getActions()) {
    			if (action.isPassword()) {
    				return action.getValue();
    			}
    		}
	    }
	    
		return "<unknown-pwd>";
	}
}
