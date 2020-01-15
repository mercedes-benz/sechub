package com.daimler.sechub.domain.scan.project;

import com.daimler.sechub.sharedkernel.type.TrafficLight;

public class ScanMockData {

	private TrafficLight result;

	public ScanMockData() {

	}

	public ScanMockData(TrafficLight result) {
		this.result=result;
	}

	public TrafficLight getResult() {
		return result;
	}

	public void setResult(TrafficLight result) {
		this.result = result;
	}
}
