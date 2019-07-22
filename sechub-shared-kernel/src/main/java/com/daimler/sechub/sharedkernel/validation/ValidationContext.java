// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

public class ValidationContext<T>{
	
	
	ValidationResult result = new ValidationResult();
	T objectToValidate;
	
	ValidationContext(T target) {
		this.objectToValidate=target;
	}

	public void addError(String error) {
		result.addError(error);
	}

	public boolean isInValid() {
		return ! result.valid;
	}

}