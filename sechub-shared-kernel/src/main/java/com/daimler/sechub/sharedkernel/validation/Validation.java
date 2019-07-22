// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

public interface Validation<T> {

	public ValidationResult validate(T target);
}
