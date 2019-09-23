package com.daimler.sechub.adapter;

import java.util.ArrayList;
import java.util.List;

public class FormScriptLoginConfig extends AbstractLoginConfig{

	private List<LoginScriptStep> steps = new ArrayList<>();

	public List<LoginScriptStep> getSteps() {
		return steps;
	}
}
