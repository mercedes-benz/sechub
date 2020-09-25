// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import java.util.Objects;

import com.daimler.sechub.commons.model.TrafficLight;

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

	@Override
	public int hashCode() {
		return Objects.hash(result);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanMockData other = (ScanMockData) obj;
		return result == other.result;
	}
	
	
}
